class Coordenador {
    private boolean ocupado = false;

    public synchronized void requisitarAcesso(String jogador) throws InterruptedException {
        while (ocupado) {
            wait(); // espera até o recurso ficar livre
        }
        ocupado = true;
        System.out.println(jogador + " está abrindo o baú...");
    }

    public synchronized void liberarAcesso(String jogador) {
        ocupado = false;
        System.out.println(jogador + " terminou de abrir o baú.");
        notifyAll();
    }
}