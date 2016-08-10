package com;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.qx.util.TPE;

public class TestJava8 {
	public static ThreadPoolExecutor es = new TPE(0, 1,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());
	public static void main(String[] args) {
		es.submit(()->slow(1));
		es.submit(()->slow(2));
		es.submit(()->slow(3));
		es.submit(()->slow(4));
		es.shutdown();
		System.out.println("done");
	}
	public static void slow(int i){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(i);
	}
	public void b(){
		List<Integer> list=new ArrayList<Integer>(); 
		   for(int i=0;i<9;i++){
		     list.add(Integer.valueOf(i));
		   }

		   System.out.println(list.stream().reduce(
		   new StringBuilder(),
		   (result,element)->
		        result=result.append(element).append(",")
		    ,(u,t) -> u=u.append(t))); //这个地方 返回 u或者t也是可以的 运行没错
		   
	}
	public void a(){
		int x;
		callIt(() -> 
			System.out.println("H222333")
		
		);
		System.out.println(sum());
	}
	public static int sum(){
		ArrayList<Block> blocks = new ArrayList<Block>();
		blocks.add(new Block(1,1));
		blocks.add(new Block(2,2));
		blocks.add(new Block(1,3));
		int sumOfWeights = blocks.stream().filter(b -> b.c == 1)
                .mapToInt(b -> b.w)
                .sum();
		return sumOfWeights;
	}

	public static void callIt(Hello h) {
		h.hello();
	}
}
