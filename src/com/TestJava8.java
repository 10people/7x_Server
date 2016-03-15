package com;
import java.util.ArrayList;

public class TestJava8 {
	public static void main(String[] args) {
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
