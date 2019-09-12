

var heads = [
		["报表日期", "周数", "城市", "总用户数", "AAA质差用户及占比", "-", "-"],
		["|", "|", "|", "|", "lost-carrier质差用户", "lost-carrier质差用户占比",
				"admin-reset质差用户"],
		["|", "|", "|", "|", "lost-carrier质差用户", "lost-carrier质差用户占比",
				"admin-reset质差用户"]]

var headsFlag = [];
$.each(heads, function(i, o) {
			headsFlag.push([]);
			$.each(heads[i], function(j, p) {
						// 初始化数组
						headsFlag[i].push({
									dir : "",
									num : 0
								});
					});
		});

// 行宽
var rowNum = 1;
// 列宽
var colNum = 1;

var x = 0;
var y = 0;

$.each(heads, function(i, o) {
			$.each(heads[i], function(j, p) {
						if (p != "|") {
							rowNum = 1;
						}
						if (p != "-") {
							colNum = 1;
						}

						if (p != "|" && p != "-") {
							x = i;
							y = j;
						}

						var row = j < heads[i].length - 1
								? (j + 1)
								: (heads[i].length - 1);
						if (heads[i][row] == "-") {
							if (headsFlag[i][j].dir == "") {
								headsFlag[i][j].dir = "-";
							}
							headsFlag[i][y].num = headsFlag[i][y].num + 1;
						} else {
							headsFlag[i][j].num = colNum;
						}

						var col = i < heads.length - 1
								? (i + 1)
								: (heads.length - 1);
						if (heads[col][j] == "|") {
							if (headsFlag[i][j].dir == "") {
								headsFlag[i][j].dir = "|";
							}
							headsFlag[x][j].num = headsFlag[x][j].num + 1;
						} else {
							headsFlag[i][j].num = rowNum;
						}

					});
		});

function getColNum(x, y) {
	var colNum = 1;
	$.each(heads, function(i, o) {
		$.each(heads[i], function(j, p) {
			if (i == x && j > y) {
                if(p == "-"){
    				colNum++;
                }else{
                    return false;
                }
			}
		});
	});
	return colNum;
}

function getRowNum(x, y) {
	var rowNum = 1;
	$.each(heads, function(i, o) {
		$.each(heads[i], function(j, p) {
			if (j == y && i > x ) { 
                if(p == "|"){
                    colNum++;
                }else{
                    return false;
                }
				rowNum++;
			}
		});
	});
	return colNum;
}

console.info(headsFlag);

// var thead = "";
// $.each(heads, function(i, o){
//    
// var tr = "<tr>"
//    
// $.each(heads[i], function(j, p){
//        
// var td = "<td>";
//        
//        
//        
// td = "</td>";
//        
// tr += td;
// });
//    
//    
// tr += "</tr>";
// });
