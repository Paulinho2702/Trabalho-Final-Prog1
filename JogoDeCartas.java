import java.util.*;

public class JogoDeCartas {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Quantos jogadores (3-6)? ");
        int numJogadores = lerNumero(scanner, 3, 6);

        if (numJogadores == -1) {
            System.out.println("Número inválido de jogadores. O jogo suporta de 3 a 6 jogadores.");
            return;
        }

        List<Jogador> jogadores = new ArrayList<>();

        for (int i = 0; i < numJogadores; i++) {
            System.out.print("Digite o nome do Jogador " + (i + 1) + ": ");
            String nomeJogador = scanner.nextLine();
            Jogador jogador = new Jogador(nomeJogador);
            jogadores.add(jogador);
        }        

        Tabuleiro tabuleiro = new Tabuleiro(jogadores);

        tabuleiro.iniciarJogo(scanner);

        System.out.println("Jogo terminado!");
        tabuleiro.mostrarPontuacoesFinais();
    }

    private static int lerNumero(Scanner scanner, int min, int max) {
        while (true) {
            if (scanner.hasNextInt()) {
                int num = scanner.nextInt();
                scanner.nextLine();  // Consumir a quebra de linha após a leitura do número
                if (num >= min && num <= max) {
                    return num;
                } else {
                    System.out.println("Número inválido. Digite um número entre " + min + " e " + max + ".");
                }
            } else {
                System.out.println("Entrada inválida. Digite um número válido.");
                scanner.nextLine(); // Consumir a entrada inválida
            }
        }
    }    
}

class Jogador {
    private String nome;
    private List<Integer> mao;
    private int pontos;
    private List<Integer> cartasColetadas;
    private List<Integer> cartasEscolhidas;

    public Jogador(String nome) {
        this.nome = nome;
        this.mao = new ArrayList<>();
        this.pontos = 0;
        this.cartasColetadas = new ArrayList<>();
        this.inicializarMao(pontos);
        this.cartasEscolhidas = new ArrayList<>();
    }

    public List<Integer> getCartasEscolhidas() {
        return cartasEscolhidas;
    }

    public void limparCartasEscolhidas() {
        cartasEscolhidas.clear();
    }

    private void inicializarMao(int rodada) {
        List<Integer> baralho = new ArrayList<>();
        for (int i = 1; i <= 109; i++) {
            baralho.add(i);
        }
        Collections.shuffle(baralho);

        mao.clear();

        if (rodada <= 1) {
            for (int i = 0; i < 11; i++) {
                if (!baralho.isEmpty()) {
                    mao.add(baralho.remove(0));
                }
            }
        }
    }

    public boolean cartaJaJogada(int numeroCarta) {
        return cartasEscolhidas.contains(numeroCarta);
    }

    public void receberCarta(Carta carta) {
        mao.add(carta.numero);
    }

    public void receberCartas(List<Carta> cartas) {
        for (Carta carta : cartas) {
            receberCarta(carta);
        }
    }

    public boolean jogarCarta(int numeroCarta, Tabuleiro tabuleiro) {
        if (!mao.contains(numeroCarta)) {
            System.out.println("Carta não encontrada. Tente novamente.");
            return false;
        }

        if (cartaJaJogada(numeroCarta)) {
            System.out.println("Você já jogou essa carta. Tente novamente.");
            return false;
        }

        System.out.println("Carta " + numeroCarta + " jogada por " + nome);
        tabuleiro.posicionarCarta(this, numeroCarta);
        cartasEscolhidas.add(numeroCarta);
        mao.remove(Integer.valueOf(numeroCarta));  // Remover a carta da mão

        return true;
    }

    public String getNome() {
        return nome;
    }

    public void mostrarMao() {
        for (int i = 0; i < mao.size(); i++) {
            System.out.println((i + 1) + ". " + mao.get(i));
        }
    }

    public void adicionarPontos(int pontos) {
        this.pontos += pontos;
    }

    public int getPontos() {
        return pontos;
    }

    public void coletarCartas(List<Integer> cartas) {
        cartasColetadas.addAll(cartas);
    }

    public List<Integer> getCartasColetadas() {
        return cartasColetadas;
    }
}

class Tabuleiro {
    private List<List<Integer>> linhas;
    private List<Integer> pontuacoes;
    private List<Jogador> jogadores;

    public Tabuleiro(List<Jogador> jogadores) {
        this.linhas = new ArrayList<>();
        this.pontuacoes = new ArrayList<>();
        this.jogadores = jogadores;

        for (int i = 0; i < 5; i++) {
            linhas.add(new ArrayList<>());
        }
    }

    private void distribuirCartasParaJogadores(Baralho baralho, int rodada) {
        if (rodada <= 1) {
            for (Jogador jogador : jogadores) {
                Carta cartaDistribuida = baralho.distribuirCarta();
                jogador.receberCarta(cartaDistribuida);
            }
        }
    }

    public void iniciarJogo(Scanner scanner) {
        Baralho baralho = new Baralho();
        baralho.createBaralho();
        baralho.embaralhaCarta();

        for (int rodada = 1; rodada <= 12; rodada++) {
            System.out.println("Rodada " + rodada);

            // Distribui 1 carta para cada jogador
            distribuirCartasParaJogadores(baralho, rodada);

            // Distribui 1 carta aberta no início de cada linha do tabuleiro apenas na primeira rodada
            if (rodada == 1) {
                for (List<Integer> linha : linhas) {
                    Carta cartaDistribuida = baralho.distribuirCarta();
                    linha.add(cartaDistribuida.numero);
                }
            }

            // Ordena as cartas iniciais nas linhas do tabuleiro
            for (List<Integer> linha : linhas) {
                Collections.sort(linha);
            }

            // Mostra o estado inicial do tabuleiro e as pontuações
            mostrarEstadoAtual();
            mostrarPontuacoes();

            // Lógica para cada jogador escolher e posicionar uma carta
            for (Jogador jogador : jogadores) {
                System.out.println("Vez de " + jogador.getNome());
                jogador.mostrarMao();
                int escolha;
                do {
                    System.out.print("Escolha uma carta para jogar: ");
                    escolha = scanner.nextInt();
                } while (!jogador.jogarCarta(escolha, this));
            }

            // Devolve as cartas escolhidas pelos jogadores para o baralho
            devolverCartasParaBaralho(baralho);

            // Atualiza as pontuações com base nas cartas no tabuleiro
            atualizarPontuacoes();

            // Mostra o estado do tabuleiro após as jogadas dos jogadores
            mostrarEstadoAtual();
            mostrarPontuacoes();
        }
    }

    private void devolverCartasParaBaralho(Baralho baralho) {
        for (Jogador jogador : jogadores) {
            List<Integer> cartasEscolhidas = jogador.getCartasEscolhidas();
            baralho.devolverCartas(cartasEscolhidas);
            jogador.limparCartasEscolhidas();
        }
    }

    public void mostrarEstadoAtual() {
        System.out.println("Estado Atual do Tabuleiro:");

        for (List<Integer> linha : linhas) {
            if (linha.isEmpty()) {
                System.out.println("Vazio");
            } else {
                for (int carta : linha) {
                    System.out.print(carta + " ");
                }
                System.out.println();
            }
        }
    }

    public void mostrarPontuacoes() {
        for (int i = 0; i < linhas.size(); i++) {
            int pontuacao = calcularPontuacao(linhas.get(i));
            pontuacoes.add(pontuacao);
            System.out.println("Pontuação da Linha " + (i + 1) + ": " + pontuacao);
        }
    }

    private boolean linhaEstaCheia(int linha) {
        return linhas.get(linha).size() >= 5;
    }

    private void atualizarPontuacoes() {
        for (int i = 0; i < pontuacoes.size() && i < linhas.size(); i++) {
            for (int j = 0; j < linhas.get(i).size(); j++) {
                Jogador jogador = encontrarJogadorPorCarta(linhas.get(i).get(j));
                if (jogador != null) {
                    jogador.adicionarPontos(pontuacoes.get(i));
                }
            }   
        }
    }

    public void posicionarCarta(Jogador jogador, int carta) {
        int linha = encontrarMelhorLinhaParaCarta(carta);
        if (linha == -1 || linhaEstaCheia(linha)) {
            System.out.println("Nenhuma linha disponível para a carta " + carta + ". Coletando todas as cartas da linha com maior número.");
            linha = encontrarLinhaComMaiorNumero();
            jogador.coletarCartas(linhas.get(linha));
            linhas.get(linha).clear();
        }

        linhas.get(linha).add(carta);
        ordenarLinha(linhas.get(linha));
    }


    private int calcularPontuacao(List<Integer> linha) {
        int pontuacao = 0;
        for (int carta : linha) {
            pontuacao += 1;

            if (carta % 10 == 5) {
                pontuacao += 1;
            }

            if (carta % 10 == 0) {
                pontuacao += 2;
            }

            if (temDoisDigitosIguais(carta)) {
                pontuacao += 4;
            }
        }
        return pontuacao;
    }

    private boolean temDoisDigitosIguais(int numero) {
        int digito1 = numero % 10;
        int digito2 = (numero / 10) % 10;
        return digito1 == digito2;
    }

    private int encontrarMelhorLinhaParaCarta(int carta) {
        int melhorLinha = -1;
        int diferencaMinima = Integer.MAX_VALUE;

        for (int i = 0; i < linhas.size(); i++) {
            if (linhas.get(i).isEmpty()) {
                return i;
            }

            int ultimaCarta = linhas.get(i).get(linhas.get(i).size() - 1);
            int diferenca = carta - ultimaCarta;

            if (diferenca >= 0 && diferenca < diferencaMinima) {
                diferencaMinima = diferenca;
                melhorLinha = i;
            }
        }

        return melhorLinha;
    }

    private int encontrarLinhaComMaiorNumero() {
        int linhaMaiorNumero = 0;
        int maiorNumero = linhas.get(0).get(linhas.get(0).size() - 1);

        for (int i = 1; i < linhas.size(); i++) {
            int ultimoNumero = linhas.get(i).get(linhas.get(i).size() - 1);
            if (ultimoNumero > maiorNumero) {
                maiorNumero = ultimoNumero;
                linhaMaiorNumero = i;
            }
        }

        return linhaMaiorNumero;
    }

    private void ordenarLinha(List<Integer> linha) {
        Collections.sort(linha);
    }

    private Jogador encontrarJogadorPorCarta(int carta) {
        for (Jogador jogador : jogadores) {
            if (jogador.getCartasColetadas().contains(carta)) {
                return jogador;
            }
        }
        return null;
    }

    public void mostrarPontuacoesFinais() {
        System.out.println("Pontuações Finais:");

        for (Jogador jogador : jogadores) {
            System.out.println("Pontuação de " + jogador.getNome() + ": " + jogador.getPontos());
        }

        System.out.println("Cartas coletadas por cada jogador:");
        for (Jogador jogador : jogadores) {
            System.out.println(jogador.getNome() + ": " + jogador.getCartasColetadas());
        }

        int menorPontuacao = Integer.MAX_VALUE;
        List<Jogador> vencedores = new ArrayList<>();

        for (Jogador jogador : jogadores) {
            if (jogador.getPontos() < menorPontuacao) {
                menorPontuacao = jogador.getPontos();
                vencedores.clear();
                vencedores.add(jogador);
            } else if (jogador.getPontos() == menorPontuacao) {
                vencedores.add(jogador);
            }
        }

        if (vencedores.size() == 1) {
            System.out.println("Parabéns! O vencedor é: " + vencedores.get(0).getNome());
        } else {
            System.out.println("Houve um empate! Os vencedores são: ");
            for (Jogador vencedor : vencedores) {
                System.out.println(vencedor.getNome());
            }
        }
    }
}

class Carta {
    public int numero;
    public Jogador dono;
    public int pontuacao;

   private final boolean usada;

    public Carta(int numero) {
        this.numero = numero;
        this.usada = false;
        verificaPontuacao(numero);
    }

    public boolean isUsada() {
        return usada;
    }

    public Carta marcarComoUsada() {
        return new Carta(numero, true);
    }

    private void verificaPontuacao(int num) {
        int pontos = 1;

        pontos += num % 10 == 5 ? 1 : 0;

        pontos += num % 10 == 0 ? 2 : 0;

        pontos += algarismosIguais(num) ? 4 : 0;

        this.pontuacao = pontos;
    }

    private boolean algarismosIguais(int num) {
        String cartaEmString = Integer.toString(num);

        if (cartaEmString.length() == 2) {
            return cartaEmString.charAt(0) == cartaEmString.charAt(1);
        } else if (cartaEmString.length() == 3) {
            return cartaEmString.charAt(0) == cartaEmString.charAt(1) || cartaEmString.charAt(1) == cartaEmString.charAt(2) || cartaEmString.charAt(0) == cartaEmString.charAt(2);
        }
        return false;
    }

    private Carta(int numero, boolean usada) {
        this.numero = numero;
        this.usada = usada;
        verificaPontuacao(numero);
    }
}

class Baralho {

    private List<Carta> cartas;

    public void createBaralho() {
        this.cartas = new ArrayList<>();
        for (int i = 1; i <= 109; i++) {
            this.cartas.add(new Carta(i));
        }

        this.embaralhaCarta();
    }

    public void embaralhaCarta() {
        Collections.shuffle(cartas);
    }

    public Carta distribuirCarta() {
        if (!cartas.isEmpty()) {
            Carta cartaDistribuida = cartas.remove(0);
            return cartaDistribuida;
        }
        return null;
    }

    public void devolverCartas(List<Integer> cartas) {
        for (Integer carta : cartas) {
            this.cartas.add(new Carta(carta));
        }
    }

    public Carta retirarCarta() {
        if (!cartas.isEmpty()) {
            Carta cartaRetirada = cartas.remove(cartas.size() - 1);
            return cartaRetirada;
        } else {
            return null;
        }
    }

    public void getTamanho() {
        System.out.println(cartas.size());
    }
}