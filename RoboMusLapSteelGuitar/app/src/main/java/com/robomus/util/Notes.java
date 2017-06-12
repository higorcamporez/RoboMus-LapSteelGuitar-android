package com.robomus.util;

/**
 * The Notes class creates and performs computations over Note objects.   
 *
 * @see Note
 * @author Leandro Lesqueves Costalonga
 * @version 1.1
 */
public class Notes {


	private static Note A = new Note("A","A");
	private static Note B = new Note("B","B");
	private static Note C = new Note("C","C");
	private static Note D = new Note("D","D");
	private static Note E = new Note("E","E");
	private static Note F = new Note("F","F");
	private static Note G = new Note("G","G");

	//Diatonic Scale: Used to find the relationship between natural notes.
	static Note notes[] = {C, D, E, F, G, A, B};

	//Cromatic Scles: Udes to deal with de flat and sharp notes. 
	static Object cromaticNotes[] = {C,"accidented note ", D,"accidented note ", E, F,
		"accidented note ", G, "accidented note ", A, "accidented note ",
		B};

	public static String getBaseNoteSymbol(Note note) {
		return note.getSymbol().substring(0,1);
	}


	// getCromaticNoteIndex recebe como par�metro um objeto do tipo
	// Note. Retorna o �ndice (0 - 11) desta Note na escala cromatica.
	/*
	 * Retorna a posi��o da Note na escala crom�tica.
	 * @param nota Note que se busca a posi��o.
	 * @return Posi��o da Note na rela��o de notas crom�ticas.
	 */
	public static int getCromaticNoteIndex(Note nota) {
		int valorAlteracao = 0;
		int indexNotaRetorno = -1;

		if (nota.isAccidental()) {
			String acc = nota.getAccident();
			if(acc.equals("#")){
				valorAlteracao = 1;
			}else{
				if(acc.equals("b")){
					valorAlteracao = -1;
				}else{
					if(acc.equals("##")){
						valorAlteracao = 2;
					}else{
						if(acc.equals("bb")){
							valorAlteracao = -2;
						}
					}
				}
			}
		}

		// Buscando a fundamental
		for (int i=0;i<12 ;i++ ) {
			if (cromaticNotes[i] instanceof Note) {

                if (nota.symbol.charAt(0) == ( ((Note) cromaticNotes[i]).symbol).charAt(0) ) {
					indexNotaRetorno += (i+1);
					break;
				}
			}
		}

		indexNotaRetorno = (indexNotaRetorno != -1)? indexNotaRetorno+=valorAlteracao: -1;

		return indexNotaRetorno;
	}
	/*
	 * Retorna a quantidade de semitons entre duas notas,
	 * podendo considerar as regi�es de oitava ou n�o.
	 * @param nota1 Note origem.
	 * @param nota2 Note destino.
	 * @param considerOctave Considera-se regi�es de oitava ou n�o.
	 * @return Quantidade de semitons entre duas notas.
	 */
	public static int getDistance(Note note1, Note note2, boolean octaveCounting)  {
		int retorno;
		int posNota1 = getCromaticNoteIndex(note1);
		int posNota2 = getCromaticNoteIndex(note2);

		if (octaveCounting) {
			int indiceNota1 = note1.getOctavePitch();
			int indiceNota2 = note2.getOctavePitch();
			int diferenca = 0;

			if (indiceNota1!=indiceNota2) {
				if (indiceNota1 < indiceNota2) {
					diferenca = indiceNota2 - indiceNota1;
					retorno=((diferenca * 12) + (posNota2 - posNota1));
				}else{
					diferenca = indiceNota1 - indiceNota2;
					retorno=((diferenca * -12) + (posNota2 - posNota1));
				}
			}else{
				retorno = posNota2 -posNota1;
			}
		}else{
			retorno = posNota2 -posNota1;
			retorno = (retorno < 0)?retorno+=12: retorno;
		}

		return retorno;
	}

	/*
	 * Verifica se a nota � alterada (D�#, Mib etc.).
	 * Ou seja verifica se na String passada como argumento existe o caracter '#' (Sustenido)
	 * ou 'b'(Bemol), indicando uma altera��o na nota.
	 * @param simbNota Note que pode ser alterada ou n�o.
	 * @return True se a nota for alterada, False caso contr�rio.
	 */
	private static boolean isAccidentalNote(String simbNota) {
		boolean retorno;
		if ((simbNota.indexOf('#')!= -1)||(simbNota.indexOf('b')!= -1)) {
			retorno = true;
		}else{
			retorno= false;
		}
		return retorno;
	}






}



