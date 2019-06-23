import os
import sys

argv= sys.argv

n =int(argv[1])+1
l = int(argv[2])
print n
print l
path = os.getcwd()
dataclass = ['amazonMR1','amazonMR2','amazonMR3','amazonMR4','amazonMR5']
pathlist = []
for dclass in dataclass:
    p = path+"/"+dclass
    pathlist.append(p)
filelist = []
wf = open("./testdata","w")
for f in dataclass:
    for fnum in range(1,n):
        fname = f + "_%s" %(fnum)
        fnum += 1
        fpath =  path +"/"+f+"/"+fname
        df = open(fpath,"r")
        lnum = 0
        for line in df:
            lnum += 1
            if lnum >= l:
                wf.write(line)
        df.close()
wf.close()
    
