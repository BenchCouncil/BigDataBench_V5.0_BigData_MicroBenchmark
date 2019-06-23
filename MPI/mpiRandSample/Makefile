Target = mpi-randsample
CC     = mpic++
CFlags = -O2 -Wall
objs = mpi-randsample.o
Include = -I./


$(Target) : $(objs)
	$(CC) -o $(Target) $(CFlags) $(objs)
mpi-randsample.o: mpi-randsample.cpp
	$(CC) -c mpi-randsample.cpp $(CFlags)


.PHONY : clean
clean:
	rm -rf *.o $(Target)
