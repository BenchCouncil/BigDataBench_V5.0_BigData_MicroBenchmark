#!/bin/bash

# Running command: ./run-fft.sh

mpirun --allow-run-as-root -n 12 ./mpifft genData-Matrix/fft-data
