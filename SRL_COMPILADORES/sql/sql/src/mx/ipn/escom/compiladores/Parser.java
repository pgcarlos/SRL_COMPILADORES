package mx.ipn.escom.compiladores;

import java.util.List;
import java.util.Stack;

public class Parser {

    private final List<Token> tokens;

    private final Token identificador = new Token(TipoToken.IDENTIFICADOR, "");
    private final Token select = new Token(TipoToken.SELECT, "select");
    private final Token from = new Token(TipoToken.FROM, "from");
    private final Token distinct = new Token(TipoToken.DISTINCT, "distinct");
    private final Token coma = new Token(TipoToken.COMA, ",");
    private final Token punto = new Token(TipoToken.PUNTO, ".");
    private final Token asterisco = new Token(TipoToken.ASTERISCO, "*");

    private final Token finCadena = new Token(TipoToken.EOF, "$");

    private int i = 0; // para entrada
    private int tope = 0; // para pila

    private Token aux; // Contiene tipo_token y lexema

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    Stack <Integer> pila = new Stack<>();

    private static final String[][] ACTION =
            {
                    // X es error
                    //       |  0    |   1   |    2     |  3  |   4  |  5   |   6  |   7  | 8 |  9 |  10 | 11  | 12  |  13 |  14 | 15  |  16 |  17 |  18
                    //        select |  from | distinct |  *  |   ,  |  id  |  .   |  $   | X |  D |  P  |  A  |  A1 |  A2 |  A3 |  T  |  T1 |  T2 |  T3
                    /* 0 */  { "s1"  ,  ""   ,    ""    ,  "" ,  ""  ,  ""  ,  ""  ,  ""  , "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 1 */  {  ""   ,  ""   ,   "s3"   , "s5",  ""  , "s8" ,  ""  ,  ""  , "", "2", "4" , "6" ,  "" , "7" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 2 */  {  ""   , "s9"  ,    ""    ,  "" ,  ""  ,  ""  ,  ""  ,  ""  , "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 3 */  {  ""   ,  ""   ,    ""    , "s5",  ""  , "s8" ,  ""  ,  ""  , "", "" , "10", "6" ,  "" , "7" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 4 */  {  ""   , "r2"  ,    ""    ,  "" ,  ""  ,  ""  ,  ""  ,  ""  , "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 5 */  {  ""   , "r3"  ,    ""    ,  "" ,  ""  ,  ""  ,  ""  ,  ""  , "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 6 */  {  ""   , "r4"  ,    ""    ,  "" ,  ""  ,  ""  ,  ""  ,  ""  , "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 7 */  {  ""   , "r7"  ,    ""    ,  "" , "s12",  ""  ,  ""  ,  ""  , "", "" ,  "" ,  "" , "11",  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 8 */  {  ""   , "r10" ,    ""    ,  "" , "r10",  ""  , "s14",  ""  , "", "" ,  "" ,  "" ,  "" ,  "" , "13",  "" ,  "" ,  "" ,  "" },
                    /* 9 */  {  ""   ,  ""   ,    ""    ,  "" ,  ""  , "s17",  ""  ,  ""  , "", "" ,  "" ,  "" ,  "" ,  "" ,  "" , "15",  "" , "16",  "" },
                    /* 10 */ {  ""   , "r1"  ,    ""    ,  "" ,  ""  ,  ""  ,  ""  ,  ""  , "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 11 */ {  ""   , "r5"  ,    ""    ,  "" ,  ""  ,  ""  ,  ""  ,  ""  , "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 12 */ {  ""   ,  ""   ,    ""    ,  "" ,  ""  , "s8" ,  ""  ,  ""  , "", "" ,  "" , "18",  "" , "7" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 13 */ {  ""   , "r8"  ,    ""    ,  "" , "r8" ,  ""  ,  ""  ,  ""  , "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 14 */ {  ""   ,  ""   ,    ""    ,  "" ,  ""  , "s19",  ""  ,  ""  , "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 15 */ {  ""   ,  ""   ,    ""    ,  "" ,  ""  ,  ""  ,  ""  , "acc", "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 16 */ {  ""   ,  ""   ,    ""    ,  "" , "s21",  ""  ,  ""  , "r13", "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" , "20",  "" ,  "" },
                    /* 17 */ {  ""   ,  ""   ,    ""    ,  "" , "r16", "s23",  ""  , "r16", "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" , "22"},
                    /* 18 */ {  ""   , "r6"  ,    ""    ,  "" ,  ""  ,  ""  ,  ""  ,  ""  , "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 19 */ {  ""   , "r9"  ,    ""    ,  "" , "r9" ,  ""  ,  ""  ,  ""  , "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 20 */ {  ""   ,  ""   ,    ""    ,  "" ,  ""  ,  ""  ,  ""  , "r11", "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 21 */ {  ""   ,  ""   ,    ""    ,  "" ,  ""  , "s17",  ""  ,  ""  , "", "" ,  "" ,  "" ,  "" ,  "" ,  "" , "24",  "" , "16",  "" },
                    /* 22 */ {  ""   ,  ""   ,    ""    ,  "" , "r14",  ""  ,  ""  , "r14", "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 23 */ {  ""   ,  ""   ,    ""    ,  "" , "r15",  ""  ,  ""  , "r15", "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" },
                    /* 24 */ {  ""   ,  ""   ,    ""    ,  "" ,  ""  ,  ""  ,  ""  , "r12", "", "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" ,  "" }
            };
    public void parse() {
        i = 0; // contador que recorre la lista de tokens
        pila.push(0); // La pila inicia en el estado 0
        aux = tokens.get(i); // Token en la posición i

        if (aux.equals(finCadena)) // si se ha recorrido toda la entrada
            System.out.println("Ninguna entrada ingresada. Intente de nuevo");
        else {
            while (true) {
                int estadoActual = pila.peek(); // El estado actual
                aux = tokens.get(i); // Token en la posición i
                int index_lexema = indexLexema(aux.tipo);

                String action = ACTION[estadoActual][index_lexema]; // Obtenemos la intersección del estado y el símbolo

                if (action.startsWith("s")) // token aceptado -> desplazamiento
                {
                    int sigEstado = Integer.parseInt(action.substring(1)); // Quitamos la s del string y el resto lo convertimos en un número
                    pila.push(sigEstado); // Agregamos a la pila el nuevo estado
                    i++; // Aumentamos i para leer el siguiente token
                }
                else if (action.startsWith("r")) // token aceptado -> reducción
                {
                    int regla = Integer.parseInt(action.substring(1)); // Quitamos la r del string y el resto lo convertimos en un número
                    reduccion(regla);
                }
                else if (action.equals("acc"))
                {
                    System.out.println("Consulta válida");
                    return; // Termina el programa
                }
                else
                {
                    error();
                    System.out.println("Consulta no válida");
                    return; // Termina el programa
                }

            }
        }
        }
        private int indexLexema(TipoToken tipo)
        {
            String t = String.valueOf(tipo); // Convertimos el tipo de token en String para compararlo
            if (t.equals("SELECT"))
                return 0;
            else if (t.equals("FROM"))
                return 1;
            else if (t.equals("DISTINCT"))
                return 2;
            else if (t.equals("ASTERISCO"))
                return 3;
            else if (t.equals("COMA"))
                return 4;
            else if (t.equals("IDENTIFICADOR"))
                return 5;
            else if (t.equals("PUNTO"))
                return 6;
            else if (t.equals("EOF"))
                return 7;
            else
                return 8; // ERROR
        }
    void reduccion(int regla) {
        String action;
        int estadoActual;
        int sig_Estado;
        switch (regla)
        {
            case 1: // D -> distinct P
                // Sacamos 2 elementos de la pila
                pila.pop();
                pila.pop();

                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][9];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            case 2: // D -> P
                // Sacamos un elemento de la pila
                pila.pop();

                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][9];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            case 3: // P -> *
            case 4: // P -> A
                // Sacamos un elemento de la pila
                pila.pop();

                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][10];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            case 5: // A -> A2 A1
                // Sacamos dos elementos de la pila
                pila.pop();
                pila.pop();

                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][11];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            case 6: // A1 -> , A
                // Sacamos dos elementos de la pila
                pila.pop();
                pila.pop();

                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][12];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            case 7: // A1 -> Ɛ
                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][12];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            case 8: // A2 -> id A3
                // Sacamos dos elementos de la pila
                pila.pop();
                pila.pop();

                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][13];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            case 9: // A3 -> . id
                // Sacamos dos elementos de la pila
                pila.pop();
                pila.pop();

                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][14];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            case 10: // A3 -> Ɛ
                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][14];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            case 11: // T -> T2 T1
                // Sacamos dos elementos de la pila
                pila.pop();
                pila.pop();

                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][15];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            case 12: // T1 -> , T
                // Sacamos dos elementos de la pila
                pila.pop();
                pila.pop();

                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][16];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            case 13: // T1 -> Ɛ
                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][16];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            case 14: // T2 -> id T3
                // Sacamos dos elementos de la pila
                pila.pop();
                pila.pop();

                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][17];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            case 15: // T3 -> id
                // Sacamos un elemento de la pila
                pila.pop();

                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][18];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            case 16: // T3 -> Ɛ
                // El nuevo estado actual
                estadoActual = pila.peek();

                // Obtenemos la intersección del estado y el símbolo
                action = ACTION[estadoActual][18];
                // Convertimos a int la intersección
                sig_Estado = Integer.parseInt(action);

                // Agregamos el nuevo estado a la pila
                pila.push(sig_Estado);
                break;
            default:
                System.out.println("Error en la reducción");
                break;
        }
    }

    private void error()
    {
        // Buscamos la posición de FROM para tenerlo como referencia
        int from_position = 0;
        for (int j = 0 ; j < tokens.size()-1; j++)
        {
            if (tokens.get(j).equals(from))
            {
                from_position = j;
                break;
            }

        }

        if (i == 0) // 'select'
            System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + "Se esperaba un select");
        // el error está antes de FROM
        else if (i<=from_position)
        {
            // select id.ERROR from | select ...id,ERROR form
            if (tokens.get(i-1).equals(punto)|| tokens.get(i-1).equals(coma))
                System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + "Se esperaba un identificador");
            // select ERROR (distinct) from | select ERROR (*) from | select ERROR (id) from
            else if (tokens.get(i-1).equals(select))
                System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + "Se esperaba un distinct, * o identificador");
            // select ... id ERROR from
            else if (tokens.get(i-1).equals(identificador))
                System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + "Se esperaba un from");
            // select distinct ERROR from
            else if (tokens.get(i-1).equals(distinct))
                System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + "Se esperaba un * o un identificador");

        }
        // Falta from
        else if (from_position == 0)
        {
            // select distinct * ERROR | select * ERROR | select ...id ERROR
            if (tokens.get(i-1).equals(asterisco) || tokens.get(i-1).equals(identificador))
                System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + "Se esperaba un from");
            // select ERROR
            else if (tokens.get(i-1).equals(select))
                System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + "Se esperaba un distinct, * o identificador");
            // select distinct ERROR
            else if (tokens.get(i-1).equals(distinct))
                System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + "Se esperaba un * o un identificador");
            // select ... id.ERROR
            else if (tokens.get(i-1).equals(punto) || tokens.get(i-1).equals(coma) )
                System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + "Se esperaba un identificador");
        }
        // El error está después de FROM
        else if (i > from_position)
        {
            // select ... from ERROR | select ... from id id, ERROR
            if (tokens.get(i-1).equals(from) || tokens.get(i-1).equals(coma))
                System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + "Se esperaba un identificador");
                // select ... from ... id id ERROR
            else if (tokens.get(i-1).equals(identificador) && tokens.get(i-2).equals(identificador))
                System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + "Se esperaba una coma");
            // select ... from id ERROR
            else if (tokens.get(i-1).equals(identificador))
                System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + "Se esperaba un identificador o una coma");

        }
        else
            System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")");
    }
}
