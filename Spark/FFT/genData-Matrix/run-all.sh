#*********************************************************************************************************************
#
#
#NOTE: 
#	$1: matrix row; 
#	$2: matrix colum;
#	$3: matrix sparse degree, $3=50 means half of matrix data is zero, $3=70 means 70% of matrix data is zero, then sparse matrix needs higher value, and dense matrix needs lower value.
#
#
#*********************************************************************************************************************


#generate matrix data 
./generate-matrix.sh $1 $2 $3

if [ $1 -eq $2 ];then
echo "square matrix $1"
#change generated data to transpose matrix (colID,rowID,value)
./change-tripleMatrix-T.sh data-kmeans matrix-$1
else
echo "non-square matrix $1 $2"
#change generated data to (rowID,colID,value)
./change-tripleMatrix.sh data-kmeans matrix-$1x$2
#change generated data to transpose matrix (colID,rowID,value)
./change-tripleMatrix-T.sh data-kmeans matrix-$1x$2-T
fi

