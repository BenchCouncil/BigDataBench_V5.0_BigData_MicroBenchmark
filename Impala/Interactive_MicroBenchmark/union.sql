insert into table result select * from 
(
select * from bigdatabench_dw_item where GOODS_AMOUNT > 750000
union all 
select * from bigdatabench_dw_item where GOODS_AMOUNT <5
) temp;
