import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

// Enum que representa o estado de um nó em relação à seção crítica (o baú)
enum Estado {
    LIBERADO,   // Não está interessado na seção crítica
    AGUARDANDO, // Quer entrar na seção crítica
    ACESSANDO   // Está atualmente na seção crítica
}

public class No implements Runnable {
    private final int id;
    private final int totalNos;
    private volatile Estado estado;
    private List<No> pares; // Lista com todos os outros nós do sistema

    // Relógio de Lamport para os timestamps das requisições
    private final AtomicInteger relogio = new AtomicInteger(0);
    private int timestampRequisicao;

    private int respostasPendentes;
    private final boolean[] respostaAdiada;
    
    // Fila para mensagens recebidas de outros nós
    private final BlockingQueue<Mensagem> filaDeMensagens = new LinkedBlockingQueue<>();

    public No(int id, int totalNos) {
        this.id = id;
        this.totalNos = totalNos;
        this.estado = Estado.LIBERADO;
        this.respostaAdiada = new boolean[totalNos];
    }

    public void definirPares(List<No> pares) {
        this.pares = pares;
    }

    // Método para que outros nós possam enviar mensagens para este nó
    public void receberMensagem(Mensagem msg) {
        filaDeMensagens.add(msg);
    }
    
    private void enviarRequisicaoParaTodos() {
        // Incrementa o relógio antes de enviar a requisição
        timestampRequisicao = relogio.incrementAndGet();
        System.out.printf("[Relógio: %d] Nó %d quer abrir o baú.\n", timestampRequisicao, id);

        respostasPendentes = totalNos - 1;

        Mensagem msgRequisicao = new Mensagem(id, timestampRequisicao, TipoMensagem.REQUISICAO);
        for (No par : pares) {
            if (par.id != this.id) {
                // "Tique" do relógio para o evento de envio
                relogio.incrementAndGet();
                par.receberMensagem(msgRequisicao);
            }
        }
    }

    private void processarMensagem(Mensagem msg) {
        // Atualiza o relógio local com base no timestamp da mensagem (Regra do Relógio de Lamport)
        relogio.set(Math.max(relogio.get(), msg.getTimestamp()) + 1);

        if (msg.getTipo() == TipoMensagem.REQUISICAO) {
            boolean temPrioridade = (msg.getTimestamp() < this.timestampRequisicao) || 
                                    (msg.getTimestamp() == this.timestampRequisicao && msg.getIdRemetente() < this.id);

            if (estado == Estado.ACESSANDO || (estado == Estado.AGUARDANDO && temPrioridade)) {
                // Adia a resposta
                respostaAdiada[msg.getIdRemetente()] = true;
                System.out.printf("[Nó %d] -> Adia resposta para a requisição do Nó %d.\n", id, msg.getIdRemetente());
            } else {
                // Envia a resposta imediatamente
                enviarResposta(msg.getIdRemetente());
            }
        } else if (msg.getTipo() == TipoMensagem.RESPOSTA) {
            respostasPendentes--;
            System.out.printf("[Nó %d] <- RESPOSTA recebida. Respostas pendentes: %d\n", id, respostasPendentes);
        }
    }

    private void enviarResposta(int idDestino) {
        // "Tique" do relógio para o evento de envio
        relogio.incrementAndGet();
        Mensagem msgResposta = new Mensagem(id, relogio.get(), TipoMensagem.RESPOSTA);
        pares.get(idDestino).receberMensagem(msgResposta);
        System.out.printf("[Nó %d] -> Envia RESPOSTA para o Nó %d.\n", id, idDestino);
    }

    private void secaoCritica() {
        System.out.printf(">>>>>>>>>> Nó %d está ABRINDO O BAÚ! <<<<<<<<<<\n", id);
        try {
            // Simula o trabalho sendo feito dentro da seção crítica
            Thread.sleep((long) (Math.random() * 1500 + 500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.printf("<<<<<<<<<< Nó %d terminou de usar o baú. >>>>>>>>>>\n", id);
    }

    @Override
    public void run() {
        try {
            // Pequena pausa para garantir que todas as threads foram inicializadas
            Thread.sleep((long) (Math.random() * 1000));
            
            while (!Thread.currentThread().isInterrupted()) {
                // Tenta entrar na seção crítica
                estado = Estado.AGUARDANDO;
                enviarRequisicaoParaTodos();

                // Espera até que todas as respostas sejam recebidas
                while (respostasPendentes > 0) {
                    processarMensagem(filaDeMensagens.take());
                }

                // --- SEÇÃO CRÍTICA ---
                estado = Estado.ACESSANDO;
                secaoCritica();
                estado = Estado.LIBERADO;
                // --- FIM DA SEÇÃO CRÍTICA ---

                // Responde a quaisquer requisições que foram adiadas
                for (int i = 0; i < totalNos; i++) {
                    if (respostaAdiada[i]) {
                        respostaAdiada[i] = false;
                        enviarResposta(i);
                    }
                }
                
                // Espera um tempo aleatório antes de tentar novamente
                Thread.sleep((long) (Math.random() * 3000 + 1000));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.printf("Nó %d foi interrompido e está encerrando.\n", id);
        }
    }
}