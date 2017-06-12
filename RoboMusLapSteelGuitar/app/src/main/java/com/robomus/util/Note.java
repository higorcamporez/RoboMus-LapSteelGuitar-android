package com.robomus.util;

public class Note  {
	/**{@value} */ 
	public static final int DOUBLE_FLAT = -2;
	/**{@value} */ 
	public static final int DOUBLE_SHARP = 2;
	/**{@value} */ 
	public static final int FLAT = -1;
	/**{@value} */ 
	public static final int NATURAL= 0;
	/**{@value} */ 
	public static final int SHARP = 1;

	/**
	 * For example, C sharp;
	 */
	protected String name;

	/**
	 * Octave region. For example , A4 = octave 4;
	 */
	protected Integer octavePitch;


	 /* For example, C#
	 */
	protected String symbol;


	private Note(){}

	/*
	 * Instancia as propriedades da nota.
	 * @param symbol S�mbolo que identifica a nota. Exemplo: a nota
	 * Mi � identificada atrav�s do simbolo "E".
	 * @param name Nome da nota. Exemplo: "Sol".
	 * @param previousInterval Interval formado com a nota natural imediatamente
	 * anterior a uma nota dada. Exemplo: Quinta justa.
	 * @param nextInterval Interval formado com a nota natural imediatamente
	 * posterior a uma nota dada. Exemplo: sexta menor.
	 */
    public Note(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public Note(String name, int octavePitch, String symbol) {
        this(name,symbol);
        this.octavePitch = octavePitch;

    }

    public Note(String symbolOctave){

        if(Character.isDigit(symbolOctave.charAt(symbolOctave.length()-1)) ){
            this.symbol = symbolOctave.substring(0,symbolOctave.length()-1);
            this.octavePitch = Integer.parseInt(symbolOctave.substring(symbolOctave.length()-1));
        }else{
            this.symbol = symbolOctave;
            this.octavePitch = 4;
        }
    }

    /*
                     * Obt�m a altera��o (sustenido ou bemol) de uma nota. Caso a nota n�o possua
                     * altera��o o retorno � null.
                     * @return Altera��o (sustenido ou bemol), ou null se n�o encontrada altera��o na
                     * nota.
                     */
	public String getAccident() {
		String retorno = null;
		if (isAccidental()) {
			retorno = symbol.substring(1);
		}
		return retorno;
	}
	 /*
	 * Obt�m o nome da nota. Exemplo: "Mi".
	 * @return Nome da nota.
	 */
	public String getName() {return name;}

	/*
	 * Obt�m a regi�o onde a nota est� localizada.
	 * @return Regi�o onde a nota est� localizada.
	 */
	public Integer getOctavePitch() {return octavePitch;}

	/*
	 * Obt�m o intervalo formado com a nota natural imediatamente anterior a uma nota dada.
	 * Exemplos: D�(nota natural imediatamente anterior) - R� = Segunda Maior
	 *           R�(nota natural imediatamente anterior) - R� sustenido = Segunda Menor
	 *           D�(nota natural imediatamente anterior) - R� bemol = Segunda Menor.
	 * @return Interval formado com a nota natural imediatamente anterior a uma nota dada.
	 */

	/*
	 * Obt�m o s�mbolo identificador da nota. Exemplo: "E".
	 * @return S�mbolo identificador de uma nota.
	 */
	public String getSymbol() {return symbol;}



	/*
	 * Verifica se uma nota possui altera��o (sustenido ou bemol).
	 * @return Verdadeiro de encontrar a altera��o e falso caso contr�rio.
	 */
	public boolean isAccidental() {
		boolean retorno;
		if ((symbol.indexOf('#')!= -1)||(symbol.indexOf('b')!= -1)){
			retorno = true;
		}else{
			retorno= false;
		}
		return retorno;
	}

	public boolean isDoubleFlat(){
		String acc = this.getAccident();
		if(acc!=null){
			return this.getAccident().equals("bb");
		}
		return false;
	}

	public boolean isDoubleSharp(){
		String acc = this.getAccident();
		if (acc != null) {
			return this.getAccident().equals("##");
		}
		return false;

	}


	public boolean isFlat(){
		String acc = this.getAccident();
		if(acc!=null){
			return this.getAccident().equals("b");
		}
		return false;
	}

	public boolean isSharp(){
		String acc = this.getAccident();
		if (acc != null) {
			return this.getAccident().equals("#");
		}
		return false;

	}

    public  double getFrequency(){

        double freq = (440 * Math.pow(2,((double)(getMidiValue() - 69)/12)));
        return freq;

    }

    public int getMidiValue()  {
        int posEscala = Notes.getCromaticNoteIndex(this);
        if (this.getOctavePitch() == 4) { //I've changed from 5 to for...not sure why it was 5 before,
                                            // but must have a good reason!
            return (60 + posEscala);
        }
        else {
            if (getOctavePitch() < 4) { //same here
                //Calcula a oitava e soma a posi��o na escala crom�tica.
                int fator = 60 - ( (4 - getOctavePitch()) * 12);
                return(fator + posEscala);
            }
            else {
                int fator = 60 + ( (getOctavePitch() - 4) * 12); //same here
                return(fator + posEscala);
            }
        }
    }

    public void setOctavePitch(Integer octavePitch) {
        this.octavePitch = octavePitch;
    }
    public int getDistanceTo(Note note){
       return Notes.getDistance(this, note, true);
    }
    public String toString(){
		return this.symbol + this.octavePitch;
	}
}
