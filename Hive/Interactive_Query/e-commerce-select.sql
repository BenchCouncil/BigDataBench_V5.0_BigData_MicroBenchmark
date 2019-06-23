drop table tmp26;
create table tmp26 as  select GOODS_PRICE,GOODS_AMOUNT from bigdatabench_dw_item where GOODS_AMOUNT > 224000 ;
