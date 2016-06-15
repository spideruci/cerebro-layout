package org.spideruci.cerebro.layout;

import java.util.Random;

import org.graphstream.ui.layout.springbox.NodeParticle;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.layout.springbox.implementations.SpringBoxNodeParticle;

public class NewSpringBox extends SpringBox {

	public NewSpringBox() {
		// TODO Auto-generated constructor stub
	}

	public NewSpringBox(boolean is3d) {
		super(is3d);
		// TODO Auto-generated constructor stub
	}

	public NewSpringBox(boolean is3d, Random randomNumberGenerator) {
		super(is3d, randomNumberGenerator);
		// TODO Auto-generated constructor stub
	}
	
	public double getK(){
		return k;
	}
	
	public double getK1(){
		return K1;
	}
	
	public double getK2(){
		return K2;
	}
	
	public void setK(double d){
		k = d;
	}
	
	public void setK1(double d){
		K1 = d;
	}
	
	public void setK2(double d){
		K2 = d;
	}
	
	@Override
	public NodeParticle newNodeParticle(String id) {
		return new NewSpringBoxNodeParticle(this, id);
	}
	

}
