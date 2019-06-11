-- 
-- update dim_compliant_d set compliant_rate = compliant_num / total_num
-- 

select 
	t.data_date,
	t.area_name,
	t.hot_num,
	t.compliant_num,
	t.total_num,
-- 	m.compliant_rate ,
	case when m.compliant_rate is null then t.compliant_rate else m.compliant_rate end as compliant_rate
from 
	dim_compliant_d t
	left join 
(select 
	a.data_date,
	a.area_name,
	a.data_month,
-- 	a.compliant_rate,
	(select sum(b.compliant_rate) from (
		select 
			substr(data_date,1,6) as data_month,
			max(data_date) as data_date,
			area_name ,
			max(compliant_rate) as compliant_rate
		from dim_compliant_d
		group by area_name, data_month
		order by data_month asc) b
		where substr(a.data_date,1,4) = substr(b.data_date,1,4)
		and a.area_name = b.area_name
	) as compliant_rate
from 
(select 
	substr(data_date,1,6) as data_month,
	max(data_date) as data_date,
	area_name ,
	max(compliant_rate) as compliant_rate
from dim_compliant_d
group by area_name, data_month
order by data_month asc) a 
order by a.area_name,a.data_date) m

on t.data_date = m.data_date and t.area_name = m.area_name

order by t.area_name,t.data_date
