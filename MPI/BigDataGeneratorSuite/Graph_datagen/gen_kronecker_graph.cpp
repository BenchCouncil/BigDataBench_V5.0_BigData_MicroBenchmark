#include "stdafx.h"
#include "kronecker.h"


int main(int argc, char* argv[]) {
  Env = TEnv(argc, argv, TNotify::StdNotify);//��ʼ����������
  Env.PrepArgs(TStr::Fmt("Kronecker graphs. build: %s, %s. Time: %s", __TIME__, __DATE__, TExeTm::GetCurTm()));
  TExeTm ExeTm;
  
  Try

  const TStr OutFNm  = Env.GetIfArgPrefixStr("-o:", "graph.txt", "Output graph file name"); 
  const TStr MtxNm = Env.GetIfArgPrefixStr("-m:", "0.9 0.5; 0.5 0.1", "Matrix (in Maltab notation)");
  const int NIter = Env.GetIfArgPrefixInt("-i:", 5, "Iterations of Kronecker product");
  const int Seed = Env.GetIfArgPrefixInt("-s:", 0, "Random seed (0 - time seed)");
  //��ȡ��������

  TKronMtx SeedMtx = TKronMtx::GetMtx(MtxNm);
  printf("\n*** Seed matrix:\n");
  SeedMtx.Dump();//�����ʼ�ľ���
  printf("\n*** Kronecker:\n");
  
  
  //Ƕ�״��뿪ʼ
  const bool IsDir = true;
  const TKronMtx& SeedGraph = SeedMtx;
  const int MtxDim = SeedGraph.GetDim();
  const double MtxSum = SeedGraph.GetMtxSum();

  const int64_t NNodes = pow(double(MtxDim), double(NIter));//MtxDim����ά��
  const int64_t NEdges = pow(double(MtxSum), double(NIter));

  FILE* F;

  printf("  FastKronecker: %lld nodes, %lld edges, %s...\n", NNodes, NEdges, IsDir ? "Directed":"UnDirected");//���Ҫ���ɵĵ����ͱ���
  TRnd Rnd(Seed);
  TExeTm ExeTm;
  // prepare cell probability vector
  TVec<TFltIntIntTr> ProbToRCPosV; // row, col position
  double CumProb = 0.0;

  for (int r = 0; r < MtxDim; r++) {
    for (int c = 0; c < MtxDim; c++) {
      const double Prob = SeedGraph.At(r, c);
      if (Prob > 0.0) {
        CumProb += Prob;
        ProbToRCPosV.Add(TFltIntIntTr(CumProb/MtxSum, r, c));
      }
    }
  }
  
  const TStr Desc = TStr::Fmt("Kronecker Graph: seed matrix [%s]", MtxNm.CStr());
  
  F = fopen(OutFNm.CStr(), "wt");
  
  if (IsDir) 
  { fprintf(F, "# Directed graph: %s \n", OutFNm.CStr()); } 
  else 
  { fprintf(F, "# Undirected graph (each unordered pair of nodes is saved once): %s\n", OutFNm.CStr()); }

  if (! Desc.Empty()) { fprintf(F, "# %s\n", Desc.CStr()); }

  fprintf(F, "# Each Number representatives a Node\n");
  
  if (IsDir)
  { fprintf(F, "# FromNodeId\tToNodeId\n"); }
  else 
  { fprintf(F, "# NodeId\tNodeId\n"); }
 

  
  // add edges
  int64_t Rng, Row, Col, n = 0;
  for (int64_t edges = 0; edges < NEdges; ) {
    Rng=NNodes;  Row=0;  Col=0;
    for (int iter = 0; iter < NIter; iter++) {
      const double& Prob = Rnd.GetUniDev();
      n = 0; while(Prob > ProbToRCPosV[n].Val1) { n++; }
      const int MtxRow = ProbToRCPosV[n].Val2;
      const int MtxCol = ProbToRCPosV[n].Val3;
      
      Rng /= MtxDim;
      Row += MtxRow * Rng;
      Col += MtxCol * Rng;
    }
      fprintf(F, "%lld\t%lld\n", Row, Col);
      edges++;
      if (! IsDir) {
        if (Row != Col) {
            fprintf(F, "%lld\t%lld\n", Col, Row);
            //Graph->AddEdge(Col, Row);
        }
        edges++;
      }
  }
  
  fclose(F);

  
  Catch
  
  printf("\nrun time: %s (%s)\n", ExeTm.GetTmStr(), TSecTm::GetCurTm().GetTmStr().CStr());//�������ʱ��
  return 0;
}
