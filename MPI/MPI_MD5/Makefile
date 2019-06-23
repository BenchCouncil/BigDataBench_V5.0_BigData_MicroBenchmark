Target = mpi-md5
CC     = mpic++
CFlags = -O2 -Wall
objs = mpi-md5.o md5.o
Include = -I./


$(Target) : $(objs)
	$(CC) -o $(Target) $(CFlags) $(objs)
mpi-md5.o: md5.h mpi-md5.cpp
	$(CC) -c mpi-md5.cpp $(CFlags)
md5.o: md5.h md5.cpp
	$(CC) -c md5.cpp $(CFlags)


.PHONY : clean
clean:
	rm -rf *.o $(Target)
