import javax.swing.*;        
import java.util.*;

public class RouterNodeLS {
  private int myID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;
  private HashMap<Integer, Integer> costosMinimos;
  private HashMap<Integer, Integer> route;
  private HashMap<Integer, Integer> vecinos;
  private HashMap<Integer, HashMap<Integer, Integer>> topology;
  private Set<Integer> paquetesRecibidos;
  private Integer sequence;
  
  //--------------------------------------------------
  public RouterNodeLS(int ID, RouterSimulator sim, HashMap<Integer,Integer> costs) {
	this.vecinos = new HashMap <Integer, Integer>();
	this.route = new HashMap <Integer, Integer>();
    this.topology = new HashMap<Integer, HashMap<Integer, Integer>>();
	
	this.paquetesRecibidos = new TreeSet<Integer>();
    this.sim = sim;
    this.myID = ID;
    this.costosMinimos = costs;
    this.sequence = 0;
    
    this.myGUI = new GuiTextArea("  Output window for Router #"+ ID + "  ");
    
    this.costosMinimos.put(myID, 0);
	this.route.put(myID, myID);    

	HashMap <Integer, Integer> aux = new HashMap<Integer, Integer>();
	aux.put(myID, 0);
	topology.put(myID, aux);

    for (Integer i : costs.keySet()){
    	if (costs.get(i) != sim.INFINITY && i != myID){
    		vecinos.put(i, costs.get(i));
			route.put(i,i);
			
			HashMap<Integer, Integer> aux1 = topology.get(i);
			if(aux1 == null) {
				aux1 = new HashMap<Integer, Integer>();
			}
			aux1.put(myID, costs.get(i));
			aux1.put(i, 0);
			topology.put(i, aux1);

			HashMap<Integer, Integer> aux2 = topology.get(myID);
			if(aux2 == null) {
				aux2 = new HashMap<Integer, Integer>();
			}
			aux2.put(i, costs.get(i));
			aux2.put(myID, 0);
			topology.put(myID, aux2);
    	}
	}   
	
	sendTopology();
	printDistanceTable();
  }

  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt) {

	    if (!paquetesRecibidos.contains(pkt.mincost.get(-1))){
			Integer v = pkt.sourceid;
	    	pkt.mincost.keySet().forEach((w) -> {
	    		if (w != -1) {
					Integer cost = pkt.mincost.get(w);
					HashMap<Integer, Integer> aux1 = topology.get(v);
					if(aux1 == null){
						aux1 = new HashMap <Integer, Integer>();					
					}
					aux1.put(w, cost);
					aux1.put(v, 0);
					topology.put(v, aux1);
					
					HashMap<Integer, Integer> aux2 = topology.get(w);
					if(aux2 == null) {
						aux2 = new HashMap <Integer, Integer> ();
					}
					aux2.put(v, cost);
					aux2.put(w, 0);
	    			topology.put(w, aux2);
	    		}
	    	});
			
			Dijkstra();

	    	paquetesRecibidos.add(pkt.mincost.get(-1));
	    	
	    	for(Integer i: vecinos.keySet()) {
				if(i != pkt.sourceid){
					RouterPacket pktNuevo = new RouterPacket(pkt.sourceid, i, pkt.mincost);
					sendUpdate(pktNuevo);
				}
	    	}
	    	/*
			myGUI.println("DEBUG RECIBE");
			myGUI.println(F.format(pkt.sourceid, 50));
			myGUI.println(F.format(pkt.mincost, 50));
			*/		
			printDistanceTable();
	    }

	    /*
		myGUI.println("DEBUG RECIBE");
		myGUI.println(F.format(pkt.sourceid, 50));
		myGUI.println(F.format(pkt.mincost, 50));
		myGUI.println(F.format(vecinos.keySet(), 50));
		*/
		
  }
  
  //--------------------------------------------------
  private void sendUpdate(RouterPacket pkt) {
	  sim.toLayer2(pkt);
	  /*
	  myGUI.println("DEBUG ENVIA");
	  myGUI.println(F.format(pkt.destid, 50));
	  myGUI.println(F.format(pkt.mincost, 50));
	  myGUI.println(F.format(vecinos.keySet(), 50));
	  */
  }
  
  private void Dijkstra() {
	  costosMinimos = new HashMap<Integer, Integer>();
	  route = new HashMap<Integer, Integer>();
	  Set<Integer> nodos = new TreeSet<Integer>();
	  nodos.add(myID);
	  costosMinimos.put(myID, 0);
	  route.put(myID, myID);
	  
	  for(Integer i: vecinos.keySet()){
		  costosMinimos.put(i, vecinos.get(i));
		  route.put(i, i);
	  }
	  
	  while(!nodos.equals(topology.keySet())) {
		  Integer w = minimo(costosMinimos, nodos);
		  
		  //para cada vecino de w
		  if (w != -1) {
			  nodos.add(w);
			  topology.get(w).forEach((v, d) -> {
				  if(!costosMinimos.containsKey(v) || costosMinimos.get(w) + d < costosMinimos.get(v)) {
					  costosMinimos.put(v, costosMinimos.get(w) + d);
					  route.put(v, w);
				  }
			  }); 
		  } else {
			  break;
		  }
	  }
  }
  
  private Integer minimo(HashMap<Integer, Integer> misCostos, Set<Integer> nodos) {
	  int keyMinimo = -1;
	  int valMinimo = sim.INFINITY;
	  for (Integer i : misCostos.keySet()) {
		  if(misCostos.get(i) < valMinimo && !nodos.contains(i)) {
			  keyMinimo = i;
		  }
	  }
	  return keyMinimo;
  }
  
  //--------------------------------------------------
  public void printDistanceTable() {
	  myGUI.println("Current table for " + myID +
			"  at time " + sim.getClocktime());
	
	  myGUI.print(F.format("Topologia", 10));
	  
	  topology.keySet().forEach((v) -> {
		  myGUI.print(F.format(v, 10));
	  });
	  
	  myGUI.println();
	  
	  topology.keySet().forEach((v) -> {
		  myGUI.print("___________");
	  });
	  
	  myGUI.println();
	  
	  for(Integer i: topology.keySet()) {
		myGUI.print(F.format(i, 10));
		myGUI.print("|");
		for(Integer j: topology.keySet()) {
			if(topology.get(i).keySet().contains(j) && topology.get(i).get(j) < sim.INFINITY){
				myGUI.print(F.format(topology.get(i).get(j), 10));
			//} else if(topology.get(j).keySet().contains(i)) {
			//	myGUI.print(F.format(topology.get(j).get(i), 10));
			} else {
				myGUI.print(F.format("inf", 10));								
			}
		}
		myGUI.println();
	  }

	  myGUI.println();
	  myGUI.println();

	  myGUI.print(F.format("", 12));
	  costosMinimos.keySet().forEach((k) -> {
		  myGUI.print(F.format(k, 10));
	  });
	  myGUI.println();
	  myGUI.print(F.format("costos", 8));
	  costosMinimos.values().forEach((k) -> {
		  if(k >= sim.INFINITY) {
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

	HashMap <Integer, Integer> aux1 = topology.get(dest);
	if(aux1 == null) {
		aux1 = new HashMap<Integer, Integer>();
	}
	aux1.put(myID, newcost);
	aux1.put(dest, 0);
	topology.put(dest, aux1);
	
	HashMap <Integer, Integer> aux2 = topology.get(myID);
	if(aux2 == null) {
		aux2 = new HashMap<Integer, Integer>();
	}	
	aux2.put(myID, 0);
	aux2.put(dest, newcost);
	topology.put(myID, aux2);

	sendTopology();	
	Dijkstra();
	
	myGUI.println("DEBUG CAMBIA");
	myGUI.println(F.format(costosMinimos, 50));
	
  }
  
  private void sendTopology(){
	HashMap <Integer, Integer> enviar = new HashMap<Integer, Integer>();
	vecinos.keySet().forEach((v) -> {
		enviar.put(v, vecinos.get(v));
	});
	Integer id = myID + 100 * sequence;
	sequence += 1;

	paquetesRecibidos.add(id);

	enviar.put(-1, id);
	enviar.put(myID, 0);

	vecinos.keySet().forEach((v) -> {
		RouterPacket pkt = new RouterPacket(myID, v, enviar);
		sendUpdate(pkt);
	});

  }
}
