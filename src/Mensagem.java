public class Mensagem {
    private final int idRemetente;
    private final int timestamp;
    private final TipoMensagem tipo;

    public Mensagem(int idRemetente, int timestamp, TipoMensagem tipo) {
        this.idRemetente = idRemetente;
        this.timestamp = timestamp;
        this.tipo = tipo;
    }

    public int getIdRemetente() {
        return idRemetente;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public TipoMensagem getTipo() {
        return tipo;
    }
}