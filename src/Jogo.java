public class Jogo {
    public static void main(String[] args) {
        Coordenador coord = new Coordenador();
        for (int i = 1; i <= 5; i++) {
            new Jogador(coord, "Jogador " + i).start();
        }
    }
}