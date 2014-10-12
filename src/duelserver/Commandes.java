package duelserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class Commandes implements Runnable
{
    private BufferedReader _in;
    private String _strCommande;
    private Thread _t;

    Commandes()
    {
        _strCommande = "";
        
        _in = new BufferedReader(new InputStreamReader(System.in));
        _t = new Thread(this);
        _t.start();
    }

    public void run()
    {
        try
        {
            while ((_strCommande = _in.readLine()) != null)
            {
                if (_strCommande.equalsIgnoreCase("quit"))
                    System.exit(0);
                else if(_strCommande.equalsIgnoreCase("total"))
                {
                    System.out.println("Nombre de connectes : "+DuelServer.getInstance().getNbClients());
                    System.out.println("--------");
                }
                else if (_strCommande.equalsIgnoreCase("parties"))
                {
                    System.out.println("Nombre de parties : "+DuelServer.getInstance().getNbGames());
                    System.out.println("--------");
                }
                else
                {
                    System.out.println("Cette commande n'est pas supportee");
                    System.out.println("Quitter : \"quit\"");
                    System.out.println("Nombre de connectes : \"total\"");
                    System.out.println("Nombre de parties : \"parties\"");
                    System.out.println("--------");
                }
                System.out.flush();
            }
        }
        catch (IOException e) {}
    }
}