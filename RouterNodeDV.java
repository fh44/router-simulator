import javax.swing.*;        
import java.util.*;

public class RouterNodeDV {
  private int myID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;
  private HashMap<Integer, Integer> costosMinimos;
  private HashMap<Integer, Integer> route;
  private HashMap<Integer, Integer> vecinos;
  boolean poison;

  //Stores for each neighbor its minimum costs
  private HashMap<Integer, HashMap<Integer, Integer>> routingTable;

  //--------------------------------------------------
  public RouterNodeDV(int ID, RouterSimulator sim, HashMap<Integer,Integer> costs) {
	this.vecinos = new HashMap <Integer, Integer>();
	this.routingTable = new HashMap<Integer, HashMap<Integer, Integer>>();
	this.route = new HashMap <Integer, Integer>();
    this.sim = sim;
    this.myID = ID;
    this.costosMinimos = costs;
	this.poison = true;
	
    this.myGUI = new GuiTextArea("  Output window for Router #"+ ID + "  ");
    
    this.costosMinimos.put(myID, 0);
    this.route.put(myID, myID);
    this.routingTable.put(myID, costosMinimos);
    

    for (Integer i : costs.keySet()){
    	if (costs.get(i) != sim.INFINITY && i != myID){
    		vecinos.put(i, costs.get(i));
    		route.put(i,i);
    	}
    }   
    vecinos.keySet().forEach((v) -> {
    	RouterPacket pkt = new RouterPacket(myID, v, costosMinimos);
    	sendUpdate(pkt);	
    });
    
    printDistanceTable();
  }

  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt) {
	  for(Integer v: costosMinimos.keySet()) {
		if(!pkt.mincost.containsKey(v)) {
			pkt.mincost.put(v, sim.INFINITY);
		}
	  }

	  for(Integer v: pkt.mincost.keySet()) {
		  if(!costosMinimos.containsKey(v)){
			  costosMinimos.put(v, costosMinimos.get(pkt.sourceid) + pkt.mincost.get(v));
		  }
	  }

	  routingTable.put(pkt.sourceid, pkt.mincost);
	  
	  calculateAndSend();
	  
	  /*
	  myGUI.println("DEBUG RECIBE");
	  myGUI.println(F.format(pkt.sourceid, 50));
	  myGUI.println(F.format(pkt.mincost, 50));
	  */

	  printDistanceTable();
  }
  
  private HashMap<Integer, Integer> clonar() {
	 HashMap<Integer, Integer> nuevo = new HashMap<Integer, Integer>();
	 costosMinimos.forEach((k,v) -> {
		 nuevo.put(k, v);
	 });
	 return nuevo;
  }
  
  
  private Boolean esIgual (HashMap<Integer, Integer> anterior) {
	  Boolean esIgual = true;
	  for(Map.Entry<Integer, Integer> entry: anterior.entrySet()){
		  Integer k = entry.getKey();
		  Integer v = entry.getValue();
		  
		  if(!costosMinimos.get(k).equals(v)){
			  esIgual = false;
		  }
	  }

	  return esIgual; 
  }
  
  
  //Calculates the minimum costs vector and returns true if it changes
  private void calculateAndSend(){
	  // clona la tabla de costos minimos
	  HashMap<Integer, Integer> costoAnterior = clonar();
	  // para cada nodo de la tabla costos minimos
	  for(Integer y: costosMinimos.keySet()){
		  Integer min = sim.INFINITY;
		  Integer vecino = -1;
		  // para cada vecino
		  for(Integer v: vecinos.keySet()){
			  Integer c;
			  c = vecinos.get(v);
			  if(routingTable.containsKey(v)){
				  // calculo el costo minimo entre el nodo y los demas
				  if(routingTable.get(v).containsKey(y) && c + routingTable.get(v).get(y) < min) {
					  min = c + routingTable.get(v).get(y);
					  vecino = v;
				  }				  
			  }
		  }
		 // actualizo la informacion con el minimo calculado
		 costosMinimos.put(y, min);
		 route.put(y, vecino);
	  }
	  
	  
	  //Recorro de nuevo los vecinos porque en la routingTable no se consideran los costos reales
	  for(Map.Entry<Integer, Integer> entry: vecinos.entrySet()) {
		  Integer v = entry.getKey();
		  Integer c = entry.getValue();

		  if(c < costosMinimos.get(v)) {
			costosMinimos.put(v, c);
		  }
	  }

	  costosMinimos.put(myID, 0);
	  route.put(myID, myID);
	  // si el costo cambio, envio el cambio a los demas nodos

	  
	  if(!esIgual(costoAnterior)) {
		  vecinos.keySet().forEach((v) -> {
			 RouterPacket pkt = new RouterPacket(myID, v, costosMinimos); 
			 sendUpdate(pkt);
		  });
	  }
  }
  
  //--------------------------------------------------
  private void sendUpdate(RouterPacket pkt) {
		
	if ((!vecinos.keySet().contains(pkt.destid) || vecinos.get(pkt.destid) < sim.INFINITY) &&
			costosMinimos.get(pkt.destid) < sim.INFINITY) {
		if (poison) {
			sim.toLayer2(mentir(pkt));
		}else {
			sim.toLayer2(pkt);			
		}

	}

	/*
	myGUI.println("DEBUG ENVIA");
	myGUI.println(F.format(pkt.destid, 50));
	myGUI.println(F.format(pkt.mincost, 50));
	*/
	
  }
  

  //--------------------------------------------------
  public void printDistanceTable() {
	  myGUI.println("Current table for " + myID +
			"  at time " + sim.getClocktime());
	  
	  Set<Integer> vertices = new TreeSet<Integer>();
	  
	  routingTable.values().forEach((v) -> {
		  v.keySet().forEach((ver) -> {
			  vertices.add(ver);
		  });
	  });
	  
	  myGUI.print(F.format("dist", 10));
	  
	  vertices.forEach((v) -> {
		  myGUI.print(F.format(v, 10));
	  });
	  
	  myGUI.println("");
	  
	  vertices.forEach((v) -> {
		  myGUI.print("___________");
	  });
	  
	  myGUI.println("");
	  
	  routingTable.keySet().forEach((k) -> {
		  HashMap<Integer, Integer> costos = routingTable.get(k);
		  myGUI.print(" nbr ");
		  myGUI.print(F.format(k, 4));
		  myGUI.print(" | ");
		  vertices.forEach((c) -> {
			  if(costos.containsKey(c)) {
				  if(costos.get(c) == sim.INFINITY){
					myGUI.print(F.format("inf", 10));					
				  } else {
					  myGUI.print(F.format(costos.get(c), 10));
				  }
			  } else {
				  myGUI.print(F.format("inf", 10));
			  }
		  });
		  myGUI.println("");
	  });

	  myGUI.println();
	  myGUI.println();

	  myGUI.print("Vector de distancia: \n");

	  myGUI.print(F.format("", 12));
	  costosMinimos.keySet().forEach((k) -> {
		  myGUI.print(F.format(k, 10));
	  });
	  myGUI.println();
	  myGUI.print(F.format("costos", 8));
	  costosMinimos.values().forEach((k) -> {
		  if(k == sim.INFINITY) {
			myGUI.print(F.format("inf", 10));			
		  } else {
			  myGUI.print(F.format(k, 10));
		  }
	  });
	  myGUI.println();
	  myGUI.print(F.format("ruta", 10));
	  route
	  .values().forEach((k) -> {
		  myGUI.print(F.format(k, 10));
	  });

	  
	  myGUI.println();
	  myGUI.println();
	  myGUI.println();
	  myGUI.println();
}

  //--------------------------------------------------
  public void updateLinkCost(int dest, int newcost) {
	  vecinos.put(dest, newcost);
	  if (newcost >= sim.INFINITY && this.routingTable.containsKey(dest)) {
		  this.routingTable.remove(dest);
	  }
  
	  calculateAndSend();
	  
	  /*
	  myGUI.println("DEBUG CAMBIA");
	  myGUI.println(F.format(vecinos, 50));	  
	  */
  }

  public RouterPacket mentir(RouterPacket i) {
	  for(Integer v: i.mincost.keySet()) {
		  if(route.get(v) == i.destid) {
			  if(v != myID) {
				  i.mincost.put(v, sim.INFINITY);
			  }
		  }
	  }
	  return i;
  }
  
}
