import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        int numeroDeNos = 5;
        List<No> nos = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(numeroDeNos);

        // Primeiro, cria todos os nós
        for (int i = 0; i < numeroDeNos; i++) {
            nos.add(new No(i, numeroDeNos));
        }

        // Agora que todos os nós existem, define a lista de pares para comunicação
        for (No no : nos) {
            no.definirPares(new ArrayList<>(nos));
            executor.submit(no); // Inicia cada nó em sua própria thread
        }

        // Deixa a simulação rodar por um tempo
        try {
            Thread.sleep(15000); // Roda por 15 segundos
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Para a simulação
        System.out.println("--- Simulação finalizada. Encerrando. ---");
        executor.shutdownNow();
    }
}