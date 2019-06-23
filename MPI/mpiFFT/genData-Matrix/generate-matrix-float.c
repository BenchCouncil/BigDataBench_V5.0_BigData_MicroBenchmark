#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
int row = 100 ; //hang
int col ;//lie
int blank = 50;//空值率
int main(int argc, char **argv)
{
	int i,j,zero=0;
	int blankRand;
	float number;
	printf ("%s",argv[1]);
	row=atoi(argv[1]);
	col=atoi(argv[2]);
	blank=atoi(argv[3]);
	FILE *fp = fopen("data-kmeans", "a+");
	if (fp==0) { printf("can't open file\n"); return 0;}
	fseek(fp, 0, SEEK_END);
	srand(time(NULL)); 
	for (i=0;i<row;i++){
		for(j=0;j<col;j++){
			number = rand()/100000000.0-9.5; 
			blankRand=rand()%101;
			if(blankRand>blank){
				fprintf(fp,"%f ", number);
			}else{
				fprintf(fp,"%d ",zero);
			}
		}
		fprintf(fp,"\n");
	}
	fclose(fp);	
	return 0;
}


