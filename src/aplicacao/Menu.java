package aplicacao;

import java.util.Scanner;

public class Menu {
	
	
	public static void Menu() {
		Principal p = new Principal();
		Scanner entrada = new Scanner(System.in);
			int valor;
					 
					  System.out.println("Digite a opção desejada 1 a 3: ");
					  valor = entrada.nextInt();
					  switch (valor)
					  {
					     case 1 :
					       System.out.println("Inserir Origem");
					       p.Origem();
					     break;
		
					     case 2 :
					    	 System.out.println("Inserir Destino");
					    	 p.Destino();
					     break;
					 
					     case 3 :
					    	 System.out.println("Inserir");
					     break;
					 
					     default :
					    	 System.out.println("digite uma informação valida!");
			  }
					
	}
}
