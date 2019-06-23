#include <stdlib.h>
#include <stdio.h>
#include <string.h>

int main (int argc,char *argv[]){
	if (argc != 3){
      	printf("please input: %s  input_file  output_file",argv[0]); 
        return 0;  
    }
    
	int x=0,x_num,y=0,count=0,n=0,num_n=0,blank=0;
    	char num[50];
    	FILE *fp,*out;
    	int ch;
    	if ((fp = fopen(argv[1],"r"))== NULL){ 
        	printf("open file %s fault",argv[1]);
        	return 0;   
    	}
    	out = fopen(argv[2], "w+");
    	while((ch=getc(fp))!=EOF){ 
		if (ch==' ' || ch=='\n'){
			num[num_n]='\0';
			if (num[0]!='\0'){
				if(num[0]=='0' && num[1]=='\0'){
					blank++;
				}else{
					fprintf(out,"%d,%d,%s\n",x,y,num);
					n++;
				}
				num[0]='\0';
				x++;
			}
			if ( ch=='\n'){
				x_num=x;
				x=0;
				y++;
			}
			num_n=0;
		}
		else {
			num[num_n]=ch;
			num_n++;
		}
        }
	printf("%d,%d,%d",x_num,y,n);
	fclose(out);
	fclose(fp);
	return 1;
}
    
    

