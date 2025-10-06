class Jogador extends Thread {
    private Coordenador coord;
    private String nome;

    public Jogador(Coordenador c, String n) {
        this.coord = c;
        this.nome = n;
    }

    @Override
    public void run() {
        try {
            coord.requisitarAcesso(nome);
            Thread.sleep(1000); // simulando tempo abrindo o baú
            coord.liberarAcesso(nome);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
