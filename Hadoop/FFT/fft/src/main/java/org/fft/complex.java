package org.fft;

public class complex {

    public double real;
    public double img;

    public void set(double a,double b)
    {
        this.real=a;
        this.img=b;
    }

    void add(complex a,complex b){
        this.real=a.real+b.real;
        this.img=a.img+b.img;
    }

    void mul(complex a,complex b){
        this.real=a.real*b.real - a.img*b.img;
        this.img=a.real*b.img + a.img*b.real;
    }

    void sub(complex a,complex b){
        this.real=a.real-b.real;
        this.img=a.img-b.img;
    }

    void divi(complex a,complex b){
        this.real=( a.real*b.real+a.img*b.img )/( b.real*b.real+b.img*b.img);
        this.img=( a.img*b.real-a.real*b.img)/(b.real*b.real+b.img*b.img);
    }

}
