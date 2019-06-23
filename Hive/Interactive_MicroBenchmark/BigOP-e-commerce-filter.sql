drop table tmp35;
create table tmp35 as select * from bigdatabench_dw_item where GOODS_AMOUNT > 750000;
