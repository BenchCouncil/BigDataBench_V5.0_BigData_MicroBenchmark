#include "mpi.h"
#include <cstdio>
#include <fstream>
#include <sstream>
#include <iomanip>
#include <iostream>
#include <vector>
#include <cstring>
#include <unistd.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <malloc.h>
#include <fcntl.h>
#include <errno.h>
#include <cstdlib>
using namespace std;

#define MAX 1024
//#define BUFSIZE 24*33+1
#define BUFSIZE 24*135723+1

static char line[BUFSIZE];
size_t offset;

int compute(char* infile, char* outfile, int myrank, int size, double sampleRatio, MPI_Status Status);

string splitStr(char* str, int p, int num)
{
	char* subStr=new char[num];
	for(int i=p;i<p+num-1;i++)
		subStr[i-p]=str[i];
	subStr[num-1]='\0';
	string result=subStr;
	delete[] subStr;
	return result;
}

int main(int argc, char *argv[])
{
    if (argc != 4) {
        fprintf(stderr, "usage: %s input output sample_ratio\n", argv[0]);
        return 1;
    }   
      
    char* infile=argv[1];
    char* outfile=argv[2];
    double begin, end, diff;
    double sampleRatio=atof(argv[3]);

    MPI_Status Status;
    MPI_Init(&argc, &argv);
    int myrank;
    int size;
    MPI_Comm_rank(MPI_COMM_WORLD,&myrank);
    MPI_Comm_size(MPI_COMM_WORLD,&size);
    begin = MPI_Wtime();
   
 
    struct stat buf;
    int result;
    result = stat(infile, &buf);    

    if (S_IFDIR & buf.st_mode) {
        if(myrank==0) cout<<"input is directory"<<endl;
	DIR * dir;
        struct dirent * ptr;
        struct stat stStatBuf;

	dir = opendir(infile);
	if (dir == NULL) {
        	printf("ERROR: Can't open dir \"%s\"\n", infile);
                return -1;
        } else {
		while ((ptr = readdir(dir)) != NULL) {
                	char Path[MAX];
			char out[MAX];
                        strcpy(Path, infile);
                        strncat(Path, "/", 1);
                        strcat(Path, ptr->d_name);
                        mkdir(outfile, S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
			strcpy(out, outfile);
			strncat(out, "/", 1);
			strcat(out, ptr->d_name);
                        if (stat(Path, &stStatBuf) == -1) {
                        	printf("Get the stat error on file:%s\n", ptr->d_name);
                                continue;
                        }
                        if (stStatBuf.st_mode & S_IFREG) {
				if(myrank==0) printf("process file　%s\n", Path);
				compute(Path,out,myrank,size,sampleRatio,Status);
			}
		}
		closedir(dir);
	}
    }
    else if (S_IFREG & buf.st_mode) {
        if(myrank==0) cout<<"input is file"<<endl;
	compute(infile,outfile,myrank,size,sampleRatio,Status);
    }

    //std::fstream fs;
     end = MPI_Wtime();
     diff = end-begin;
     if(myrank==0) cout<<"time="<<diff<<endl;

     MPI_Finalize();
     return 0;
}   

int compute(char* infile, char* outfile, int myrank, int size, double sampleRatio, MPI_Status Status){ 
    int fd = open(infile,O_RDONLY);
    ofstream outFile(outfile);
    vector<char*> textLine;
    vector<string> sampleVec;
    double sampleVal;
    ostringstream oss;
    char *remain;
    int bytes_read;
    char *p;
    const char *split="\n";
    int lineNum=0;
    int len;

    if(fd < 0)
    {
	cout<<"cannot open input file"<<endl;
	return 1;
    }
    if(!outFile)
    {
	cout<<"cannot open output file"<<endl;
	return 1;
    }

    while ((bytes_read = read(fd, &line[offset], BUFSIZE-1-offset)))
    {
        if ((bytes_read == -1) && (errno != EINTR)) 
        {
            cout<<"read error"<<endl;
            return 1;
        }
        else if (bytes_read > 0) {
            //if(myrank==0) cout<<line[bytes_read+offset-1]<<endl;
            line[bytes_read+offset]='\0';
            if((remain=strrchr(line,'\n'))!=NULL)
            {
                remain=remain+1;
                if(remain[0]!='\0')
                {
                    len=bytes_read+offset-strlen(remain)-1;
                    //remain=remain+1;
              //      if(myrank==0) cout<<"middle111 remain"<<remain<<endl;
                    line[len]='\0';
                //    if(myrank==0) cout<<"middle remain222"<<remain<<endl;
                }
            }
            else
            {
                if(myrank==0)
                    cout<<"bufsize too small"<<endl;
                return 1;
            }
            p=strtok(line,split);
            while(p)
            {
                textLine.push_back(p);
                lineNum++;
                p=strtok(NULL,split);
            }
        }
        for(int j=0;j<2;j++)
        {
            for(unsigned int i=myrank;i<textLine.size();i+=size)
            {
 		srand((unsigned)time(NULL));
                sampleVal = rand()/double(RAND_MAX);
                if(j==0 && sampleVal < sampleRatio)
                    sampleVec.push_back(textLine[i]);
            }
        }
        if(remain != NULL && remain[0] !='\0')
        {
            offset=strlen(remain);
            strcpy(line,remain);
        }
        else
        {
            offset=0;
        }
        //if(myrank==0) cout<<"after line="<<line<<", offset="<<offset<<", remain="<<remain<<endl;
        p=NULL;
        remain[0]='\0';
        textLine.clear();
        //if(myrank==0)
          //  cout<<"after: offset="<<offset<<", line="<<line<<endl;
    }
    //for(int i=0;i<size;i++)
    //{
      //  if(myrank==i)
        //{
          //  cout<<"myrank="<<myrank<<",lineNum="<<lineNum<<",sampleVal="<<sampleVec.size()<<endl;
        //}
        //sleep(1);
    //}

     if(myrank==0)
		cout<<"random sample compute finish"<<endl;
 

     vector<string> allStr;
     if(myrank==0)
	     cout<<"data transfer begin:"<<endl;
//     for(int i=0;i<lineNum/size;i++)
    
     int sampleVecCount=sampleVec.size();
     int *receiveCountVec=new int[size];
     MPI_Allgather(&sampleVecCount,1,MPI_INT,receiveCountVec,1,MPI_INT,MPI_COMM_WORLD);

     for(int i=0;i<receiveCountVec[size-1];i++)
     {
	     int count=sampleVec[i].size()+1;
	     char* sendBuf=new char[count];
	     strcpy(sendBuf,sampleVec[i].c_str());
	     int *transNum=new int[size];
	     MPI_Gather(&count,1,MPI_INT,transNum,1,MPI_INT,0,MPI_COMM_WORLD);
	     int *displs=new int[size];
	     displs[0]=0;
	     for(int j=1;j<size;j++)
	     {
		     displs[j]=displs[j-1]+transNum[j-1];
	     }
	     int charNum=0;
	     if(myrank==0)
	    {
		     for(int j=0;j<size;j++)
			     charNum += transNum[j];
	    }
	     char* recev=new char[charNum];
	     MPI_Gatherv(sendBuf,count,MPI_CHAR,recev,transNum,displs,MPI_CHAR,0,MPI_COMM_WORLD);
	     if(myrank==0)
	     {
		     for(int j=0;j<size;j++)
		     {
			     string tmp=splitStr(recev,displs[j],transNum[j]);
			     allStr.push_back(tmp);
		     }
	     }
	     delete[] recev;
	     delete[] sendBuf;
	     delete[] transNum;
	     delete[] displs;
     }
	

     MPI_Barrier(MPI_COMM_WORLD);
	 if( myrank==0 && (receiveCountVec[0]>receiveCountVec[size-1]))
     {
         for(int i=receiveCountVec[size-1];i<receiveCountVec[0];i++)
             allStr.push_back(sampleVec[i]);
     }

     for(int i=1;i<size-1;i++)
     {
         if(i==myrank && (receiveCountVec[i]>receiveCountVec[size-1]))
         {
             for(int k=receiveCountVec[size-1];k<receiveCountVec[i];k++)
             {
                int count=sampleVec[k].size()+1;
                char* sendBuf=new char[count];
                strcpy(sendBuf,sampleVec[k].c_str());
                MPI_Send(sendBuf,count,MPI_CHAR,0,myrank,MPI_COMM_WORLD);
                delete[] sendBuf;
             }
         }
     }

     if(myrank==0){
         int remainCount=0;
         for(int i=1;i<size-1;i++)
             if(receiveCountVec[i]>receiveCountVec[size-1])
                 remainCount++;
         for(int k=1;k<=remainCount;k++)
         {
             char* recvBuf=new char[MAX];
             MPI_Recv(recvBuf,MAX,MPI_CHAR,k,k,MPI_COMM_WORLD,&Status);
             string tmp=recvBuf;
             allStr.push_back(tmp);
             delete[] recvBuf;
         }
     }
     delete[] receiveCountVec;

     if(myrank==0)
	     cout<<"data transfer end."<<endl;
	
     if(myrank==0)
     {
	     cout<<"data output begin"<<endl;
	     for(unsigned int j=0;j<allStr.size();j++)
	     {
 		     oss<<resetiosflags(ios::right)<<allStr[j]<<endl;
 		     outFile<<oss.str();
 		     oss.str("");
	     }
	     cout<<"data outfile end"<<endl;
     }
}
