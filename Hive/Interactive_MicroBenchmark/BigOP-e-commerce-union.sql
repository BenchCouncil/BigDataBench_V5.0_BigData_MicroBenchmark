drop table tmp38;
create table tmp38 as select * from 
(
select * from bigdatabench_dw_item where GOODS_AMOUNT > 750000
union all 
select * from bigdatabench_dw_item where GOODS_AMOUNT <5
) temp;
