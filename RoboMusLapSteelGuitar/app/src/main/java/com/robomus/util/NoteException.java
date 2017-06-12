package com.robomus.util;

/*
 * Estende a classe InvalidChordException e trata as exce��es lan�adas para uma
 * nota inv�lida quando forem identificadas irregularidades quanto a nota��o utilizada.
 * Exemplo: o s�mbolo que representa a nota L� � "A" e o usu�rio decide representar com "ZZ".
 * Obs: O usu�rio pode redefinir toda a nota��o se quiser, mas n�o pode redefinir
 * s�mbolos individualmente. S� � poss�vel trocar toda a nota��o de uma vez.
 * @see InvalidChordException
 * @see Exception
 * @author Leandro Lesqueves Costalonga
 * @version 1.1
 */

/**
 * Thrown when there is a problem instantiation a Note object.
 */
public class NoteException extends Exception {
  private String note;

/*
 * Constr�i uma Exception para Nota Inv�lida que recebe tr�s argumentos atrav�s
 * dos par�metros, a mensagem a ser exibida ao usu�rio, a cifra, e a nota.
 * @param mensagem Mensagem a ser exibida ao usu�rio quando uma exce��o ocorrer.
 * @param cifra Cifra que possui uma nota inv�lida.
 * @param nota Nota inv�lida.
 */
  public NoteException(String message,  String note) {
    super(message);
    this.note = note;
  }

/*
 * Retorna a nota inv�lida.
 * @return Nota inv�lida.
 */
  public String getInvalidNote() {
    return note;
  }

}
