drop table tmp27;
create table tmp27 as select GOODS_ID, sum(GOODS_NUMBER) from bigdatabench_dw_item group by GOODS_ID ;
