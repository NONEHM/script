/*
    <!-- sftp连接工具 -->
    <dependency>
        <groupId>com.jcraft</groupId>
        <artifactId>jsch</artifactId>
        <version>0.1.54</version>
    </dependency>
 */

package com.hm.sftp;


import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Sftp工具类
 *
 * @author Maple
 * @since 2019-12-01
 */
public class SftpUtil {
    private static Logger log = LoggerFactory.getLogger(SftpUtil.class);

    private String host;
    private String username;
    private String password;
    private int port = 22;

    private ChannelSftp sftp = null;
    private Session sshSession = null;

    public SftpUtil() {
    }

    public SftpUtil(String host, int port, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    public SftpUtil(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        SftpUtil sftp = null;
        // 本地存放地址
        String localPath = "E:\\tmp";

        // Sftp下载路径
        String sftpPath = "/home/xxx/";
        List<String> filePathList = new ArrayList<String>();
        try {
            sftp = new SftpUtil("www.xxx.com", 22, "xxx", "xxx");
            sftp.connect();
            // 下载
            sftp.downloadFiles(sftpPath, localPath, "abc", ".txt", false);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sftp.disconnect();
        }
    }

    public static Logger getLog() {
        return log;
    }

    public static void setLog(Logger log) {
        SftpUtil.log = log;
    }

    /**
     * * 通过SFTP连接服务器
     */
    public boolean connect() {
        JSch jsch = new JSch();
        try {
            sshSession = jsch.getSession(username, host, port);

            sshSession.setPassword(password);

            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            if (log.isInfoEnabled()) {
                log.info("jsch Session connected.");
            }

            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            log.info("jsch Opening sftp Channel.");
            sftp = (ChannelSftp) channel;
            log.info("Connected to " + host + ".");
        } catch (JSchException e) {
            log.error("sftp connect error! ", e);
        }

        return true;
    }

    /**
     * * 关闭连接
     */
    public void disconnect() {
        if (this.sftp != null) {
            if (this.sftp.isConnected()) {
                this.sftp.disconnect();
                if (log.isInfoEnabled()) {
                    log.info(" sftp is closed");
                }
            }
        }
        if (this.sshSession != null) {
            if (this.sshSession.isConnected()) {
                this.sshSession.disconnect();
                if (log.isInfoEnabled()) {
                    log.info(" sshSession is closed");
                }
            }
        }
    }

    /**
     * 批量下载文件
     *
     * @param remotePath 远程下载目录(以路径符号结束,可以为相对路径eg: /home/sftp/2014/)
     * @param localPath  本地保存目录(以路径符号结束,D:\downloac\sftp\)
     * @param prefix     文件名开头
     * @param suffix     文件名结尾
     * @param delRemote  下载后是否删除sftp文件
     * @return
     */
    public List<String> downloadFiles(String remotePath, String localPath, String prefix, String suffix, boolean delRemote) {
        List<String> downFiles = new ArrayList<String>();
        try {
            Vector v = listFiles(remotePath);

            if (v.size() > 0) {
                prefix = prefix == null ? "" : prefix.trim();
                suffix = suffix == null ? "" : suffix.trim();

                boolean isDown;
                Iterator it = v.iterator();
                while (it.hasNext()) {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) it.next();
                    String filename = entry.getFilename();
                    SftpATTRS attrs = entry.getAttrs();
                    if (!attrs.isDir()) {
                        File localFile = new File(localPath, filename);
                        // 验证开头和结尾
                        if ((prefix.equals("") || filename.startsWith(prefix)) && (suffix.equals("") || filename.endsWith(suffix))) {
                            isDown = downloadFile(remotePath, filename, localFile);
                            if (isDown) {
                                downFiles.add(localFile.getName());
                                if (delRemote)
                                    deleteSFTP(remotePath, filename);
                            }
                        }
                    }
                }

                if (log.isInfoEnabled()) {
                    log.info("file download success, file size : {}", downFiles.size());
                }
            }
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            this.disconnect();
        }
        return downFiles;
    }

    /**
     * 下载单个文件
     *
     * @param remotePath     远程文件路径
     * @param remoteFileName 远程文件名称
     * @param localFile      本地文件
     * @return 是否下载成功
     */
    public boolean downloadFile(String remotePath, String remoteFileName, File localFile) {
        try {
            sftp.get(remotePath + remoteFileName, localFile.getAbsolutePath());
            log.info("=== file.download: [{}] success.", remoteFileName);
            return true;
        } catch (SftpException e) {
            log.error("sftp download file error!", e);
//            if (e.getMessage().toLowerCase().equals("no such file")) {
//                if (log.isDebugEnabled()) {
//                    log.debug("=== file.download.error: [{}], {}.", remoteFileName, e.getMessage());
//                }
//            } else
//                log.error("=== file.download.error: [{}], {}.", remoteFileName, e.getMessage());
//            localFile.delete();
        }
        return false;
    }

    /**
     * 上传单个文件
     *
     * @param remotePath     远程保存目录
     * @param remoteFileName 保存文件名
     * @param localFile      上传的文件名
     * @return
     */
    public boolean uploadFile(String remotePath, String remoteFileName, String localFile) {

        try {
            FileInputStream fileInputStream = new FileInputStream(localFile);
            cdDir(remotePath);
            sftp.put(fileInputStream, remoteFileName);
            if (log.isInfoEnabled()) {
                log.info(" file.upload: [{}] success.", localFile.substring(localFile.lastIndexOf(File.separator)));
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 批量上传文件
     *
     * @param remotePath 远程保存目录
     * @param localPath  本地上传目录(以路径符号结束)
     * @param delLocal   上传后是否删除本地文件
     * @return
     */
    public List<String> uploadFiles(String remotePath, String localPath, String prefix, String suffix, boolean delLocal) {
        List<String> upfiles = new ArrayList<String>();
        try {
            File file = new File(localPath);
            File[] files = file.listFiles();

            prefix = prefix == null ? "" : prefix.trim();
            suffix = suffix == null ? "" : suffix.trim();

            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if ((prefix.equals("") || fileName.startsWith(prefix)) && (suffix.equals("") || fileName.endsWith(suffix))) {
                    if (files[i].isFile()) {
                        boolean isUpload = this.uploadFile(remotePath, fileName, files[i].getAbsolutePath());
                        if (isUpload) {
                            upfiles.add(files[i].getAbsolutePath());
                            if (delLocal)
                                deleteLocal(files[i]);
                        }
                    }
                }
            }

            if (log.isInfoEnabled()) {
                log.info(" file upload success，file size : {}", upfiles.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.disconnect();
        }

        return upfiles;

    }

    /**
     * 删除本地文件
     *
     * @param file 文件对象
     * @return
     */
    public boolean deleteLocal(File file) {
        if (!file.exists()) {
            return false;
        }

        if (!file.isFile()) {
            return false;
        }

        boolean rs = file.delete();
        if (rs && log.isInfoEnabled()) {
            log.info(" file.delete.success.");
        }
        return rs;
    }

    /**
     * 创建目录
     *
     * @param createpath
     * @return
     */
    public boolean cdDir(String createpath) {
        try {

            String pwd = this.sftp.pwd();
            if (pwd.contains("/" + createpath + "/"))
                return true;

            if (isDirExist(createpath)) {
                this.sftp.cd(createpath);
                return true;
            }
            String[] pathArry = createpath.split("/");
            StringBuffer filePath = new StringBuffer("/");
            for (String path : pathArry) {
                if (path.equals("")) {
                    continue;
                }
                filePath.append(path + "/");
                if (isDirExist(filePath.toString())) {
                    sftp.cd(filePath.toString());
                } else {
                    // 建立目录
                    sftp.mkdir(filePath.toString());

                    // 进入并设置为当前目录
                    sftp.cd(filePath.toString());
                }

            }

            pwd = this.sftp.pwd();
            if (pwd.contains("/" + createpath + "/"))
                return true;
        } catch (SftpException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断目录是否存在
     *
     * @param directory 目录
     * @return
     */
    public boolean isDirExist(String directory) {
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpATTRS = sftp.lstat(directory);
            isDirExistFlag = true;
            return sftpATTRS.isDir();
        } catch (SftpException e) {
            // 没有这样的文件
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }

    /**
     * 删除stfp文件
     *
     * @param directory  要删除文件所在目录
     * @param deleteFile 要删除的文件
     */
    public void deleteSFTP(String directory, String deleteFile) {
        try {
            // sftp.cd(directory);
            sftp.rm(directory + deleteFile);
            if (log.isInfoEnabled()) {
                log.info("delete file success from sftp.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果目录不存在就创建目录
     *
     * @param path 目录
     */
    public void mkdirs(String path) {
        File f = new File(path);

        String fs = f.getParent();

        f = new File(fs);

        if (!f.exists()) {
            f.mkdirs();
        }
    }

    /**
     * 列出目录下的文件
     *
     * @param directory 要列出的目录
     * @return
     * @throws SftpException
     */
    public Vector listFiles(String directory) throws SftpException {
        return sftp.ls(directory);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ChannelSftp getSftp() {
        return sftp;
    }

    public void setSftp(ChannelSftp sftp) {
        this.sftp = sftp;
    }

    public Session getSshSession() {
        return sshSession;
    }

    public void setSshSession(Session sshSession) {
        this.sshSession = sshSession;
    }
}
