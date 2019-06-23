#include "fftw3-mpi.h"
#include <iostream>
#include <fstream>
#include <sstream>
#include <cmath>

using namespace std;

void initial(ifstream &data, double *in, ptrdiff_t alloc_local, ptrdiff_t local_start, ptrdiff_t local_m, unsigned n, unsigned t)
{
    //double *matrix = new double[alloc_local]();
    unsigned i,j;
    double key;
    cout<<"initial data"<<endl;
    for(unsigned line=0; line!=t; line++)
    {
        data >> i >> j >> key;
//        cout << i<<j<<key<<endl;
	if(i>=(local_start+1) && i<=(local_start+local_m))
		in[(i-local_start-1)*n+j-1] = key;
    }
}

int main(int argc, char *argv[])
{
    if(argc!=2)
    {
        cerr << "Wrong number of arguments" << endl;
        cout << "Usage: mpirun -n [numofprocs] ./[executable_file] [data_file]" << endl;
        return -1;
    }

    ifstream data(argv[1]);
    if(!data)
    {
        cerr << "Can't open file!" << endl;
        return -1;
    }

    unsigned m,n,t;
    data >> m >> n >> t;
    int myRank,
        numProcs;

    fftw_plan plan;
    ptrdiff_t alloc_local,
           local_m,
           local_start,
           i,
           j,
           key;

    MPI_Init(&argc, &argv);
    fftw_mpi_init();

   MPI_Comm_rank(MPI_COMM_WORLD, &myRank);
   MPI_Comm_size(MPI_COMM_WORLD, &numProcs);

    alloc_local = fftw_mpi_local_size_2d(m, n,
                MPI_COMM_WORLD, &local_m, &local_start);
  //  cout << local_start << endl;
    double *in = new double[alloc_local]();
    double *out = new double[alloc_local]();
    cout<<"fftw plan"<<endl;
    plan = fftw_mpi_plan_r2r_2d(m, n, in, out, MPI_COMM_WORLD,
                                FFTW_R2HC, FFTW_R2HC,  FFTW_ESTIMATE);
    
    initial(data, in, alloc_local, local_start, local_m, n, t),
    cout<<"fftw execute"<<endl;
    fftw_execute(plan);
    cout<<"complete plan"<<endl;
    fftw_destroy_plan(plan);

    //for(i=0; i!=local_m; i++)
    //    for(j=0; j!=n; j++)
    //        cout << out[i*n+j] << endl;

//    delete [] in;
//    delete [] out;
    MPI_Finalize();

    return 0;
}
