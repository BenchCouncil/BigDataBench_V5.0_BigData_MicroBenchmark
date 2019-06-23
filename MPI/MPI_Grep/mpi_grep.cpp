#define _LARGEFILE_SOURCE
#define _LARGEFILE64_SOURCE
#define _FILE_OFFSET_BITS 64


#include "mpi.h"
#include <stdio.h>      /* printf, fopen */
#include <stdlib.h>     /* exit, EXIT_FAILURE */
#include <string.h>

#include <unistd.h>
#include <fcntl.h>
#include <dirent.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <errno.h>

#include <iostream>
#include <string>



#define BUFSIZE 1024
#define MAX_PATH 1024

int is_match(const std::string& text, const std::string& pattern)
{
	// FIXME TODO use actual (posix?) regex, boost regex, regex++ or whatnot
	size_t pos = text.find(pattern, 0);
	int sub_found_cnt = 0;
	while(pos != std::string::npos)
	{
		//	positions.push_back(pos);
		pos = text.find(pattern,pos+1);
		sub_found_cnt++;
	}
	return sub_found_cnt;
}

void my_error(const char* errstring, int line)
{
	fprintf(stderr,"line:%d",line);
	perror(errstring);
	exit(1);
}

unsigned long get_file_size(int fd)
{
	unsigned long file_size;

	if((lseek(fd,0,SEEK_END)) == -1)
	{
		my_error("lseek",__LINE__);
	}
	if((file_size  = lseek(fd,0,SEEK_CUR)) == -1)
	{
		my_error("lseek",__LINE__);
	}
	if((lseek(fd,0,SEEK_SET)) == -1) 
	{
		my_error("lseek",__LINE__);
	}

	//	if((ret = read(fd,buf,len)) == -1)
	//	{
	//		my_error("read",__LINE__);
	//	}
	//
	//	for(i=0;i<len;i++)
	//	{
	//		printf("%c",buf[i]);
	//	}
	//	printf("\n");

	return file_size;
}


int processFile(const char *fileName, const char *pattern_cstr, unsigned long &match_count, int &rank, int &size){
	int from_fd;
	int bytes_read;
	char line[BUFSIZE];

	if ((from_fd = open(fileName, O_RDONLY|O_LARGEFILE)) == -1) {
		fprintf(stderr, "Open %s Error %s\n", fileName, strerror(errno));
		exit(1);
	}


#ifdef DEBUG
	std::cout<<"End of open File \n";
#endif

	unsigned long file_size = 0;
	file_size = get_file_size(from_fd);

#ifdef DEBUG
	printf("File Size:%d\n",file_size);
#endif

	unsigned long file_part_thr = 0;
	file_part_thr = file_size / size / BUFSIZE ;

#ifdef DEBUG
	printf("File Part Size:%d\n",file_part_thr );
#endif

	std::string pattern(pattern_cstr);
#ifdef DEBUG
	std::cout<<"Pattern:"<<pattern<<std::endl;
#endif
	if((lseek(from_fd,file_size / size * rank ,SEEK_SET)) == -1) 
	{
		my_error("lseek",__LINE__);
	}
	while (bytes_read = read(from_fd, line, BUFSIZE-1) && file_part_thr >  0) {
		if ((bytes_read == -1) && (errno != EINTR)) break;
		else if (bytes_read > 0) {
			//ptr = line;

			if (line[strlen(line) - 1] == '\n')
				line[strlen(line)-1] = '\0';
			//char* new_line = new char[strlen(line) + 1];
			//strcpy(new_line, line);
			if (line!=NULL)
			{
				std::string str(line);
#ifdef DEBUG
				std::cout<<"HHH: "<<str<<" HHH"<<std::endl;
#endif
				match_count +=	is_match(str, pattern);


				//delete new_line;
				file_part_thr--;
			}
		}
	}


#ifdef DEBUG
	std::cout<<"End of process File \n";
#endif

	close(from_fd);

#ifdef DEBUG
	std::cout<<" End of close File \n"<<std::endl;
#endif
	return 1;
}


int main(int argc, char* argv[])
{
	switch(argc)
	{
		case 0: case 1: case 2:
			std::cerr << "Useage: ./mpi_grep [input_file] [pattern]" << std::endl;
			return 254;
		case 3:
			break;
		default:
			std::cerr << "Useage: ./mpi_grep [input_file] [pattern]" << std::endl;
			return 255;
	}
	int rank, size, alpha_index_begin, alpha_index_end;
	char   processor_name[MPI_MAX_PROCESSOR_NAME];
	int    namelen;
	double startwtime = 0.0, endwtime;
	MPI_Status mpi_status;

	MPI_Init(&argc,&argv); /* starts MPI */
	MPI_Comm_rank(MPI_COMM_WORLD, &rank); /* get current process id */ 
	MPI_Comm_size(MPI_COMM_WORLD, &size); /* get number of processes */

	MPI_Get_processor_name(processor_name,&namelen);

	fprintf(stdout,"Process %d of %d is on %s\n",rank, size, processor_name);
	fflush(stdout);

	if (rank == 0)
		startwtime = MPI_Wtime();

	unsigned long match_count = 0;



	
	char* fileName = argv[1]; 
	struct stat buf; 
	int result; 
	result = stat( fileName, &buf ); 
	
	if(S_IFDIR & buf.st_mode){ 

		printf("##folder\n"); 

		char * pFilePath = argv[1];
		DIR * dir;
		struct dirent * ptr;
		struct stat stStatBuf;

		//	chdir(pFilePath);
		dir = opendir(pFilePath);
		if(dir == NULL){
			printf("ERROR: Can't open dir \"%s\"\n",pFilePath);
			return 0;
		} else {
			while ((ptr = readdir(dir)) != NULL){
				char Path[MAX_PATH];
				strcpy(Path, pFilePath);
				strncat(Path, "/", 1);
				strcat(Path, ptr->d_name);
				if (stat(Path, &stStatBuf) == -1){
					printf("Get the stat error on file:%s\n", ptr->d_name);
					continue;
				}
				if (stStatBuf.st_mode & S_IFREG){
//					strcpy(Path, pFilePath);
//					strncat(Path, "/", 1);
//					strcat(Path, ptr->d_name);
					printf("process file　%s\n", Path);
					processFile( Path,argv[2],match_count,rank,size);

				}
				//this must change the directory , for maybe changed in the recured function
				//			chdir(pFilePath);
			}
			closedir(dir);
		}

	}else if(S_IFREG & buf.st_mode){ 
		printf("###file\n");
		processFile( argv[1],argv[2],match_count,rank,size);
	} 

	
	
	

	unsigned long final_total_cnt;

	MPI_Reduce(&match_count, &final_total_cnt, 1, MPI_LONG_LONG_INT, MPI_SUM, 0, MPI_COMM_WORLD); 


#ifdef DEBUG
	std::cout<<"Rank: "<<rank<<"End of Reduce"<<std::endl;
#endif

	if (rank == 0){
		//fprintf(stderr,"Total words %d Diff cnt %d Max Cnt %d\n",
		//final_total_cnt, final_diff_cnt, final_max_cnt);
		std::cout<<"Total Count "<<final_total_cnt<<std::endl;

		endwtime = MPI_Wtime();
		std::cout<<"wall clock time = "<<endwtime-startwtime<<std::endl;
		//printf("wall clock time = %f\n", endwtime-startwtime);	       
		//fflush(stdout);
	}  

#ifdef DEBUG
	std::cout<<"Rank: "<<rank<<"End of Print"<<std::endl;
#endif

	MPI_Finalize(); 

#ifdef DEBUG
	std::cout<<"Rank: "<<rank<<"End of MPI_Finalize"<<std::endl;
#endif
	return 0;

}
