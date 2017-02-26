package cybercycles;

import java.util.Random;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AI {

    /* Configuration */
    public final String ROOM = "VTC";
    public final String TEAM = "VTC2";
    
    //global game variables
    private int board[][]; //0 blank, 1-4 player x, 5 obstacle
    private int coords[][]; //0-3 player x -1, x or y coord (x:0, y:1)
    private int width;
    private int height;
    private int me;
    private String teamId[];
    private String dir[]; //contains direction of players 0-3, value of u, d, l, or r
    
    

    /* Déplacement de l'A.I. */
    public final char[] directions = {'u', 'l', 'd', 'r'};
    public char direction;

    Random random = new Random();

    /**
     * Fonction appelée en début de partie.
     *
     * @param config Configuration de la grille de jeu
     * @throws org.json.JSONException
     */
   
    public void start(JSONObject config) throws JSONException {
        JSONArray players = config.getJSONArray("players");
        this.coords = new int[4][2];
        this.teamId = new String[4];
        this.dir = new String[4];
        for (int i = 0; i < 4; i++ ){
            int id = players.getJSONObject(i).getInt("id");
            this.coords[id-1][0] = players.getJSONObject(i).getInt("x");
            this.coords[id-1][1] = players.getJSONObject(i).getInt("y");
            this.teamId[id-1] = players.getJSONObject(i).getString("team");
            this.dir[id-1] = players.getJSONObject(i).getString("direction");
        }
        this.width = config.getInt("w");
        this.height = config.getInt("h");
        this.me = Integer.parseInt(config.getString("me"));
 
        board = new int[width][height];
        JSONArray obstacles = config.getJSONArray("obstacles");
        for(int i = 0; i < obstacles.length(); i++){
            int x = obstacles.getJSONObject(i).getInt("x");
            int y = obstacles.getJSONObject(i).getInt("y");
            int w = obstacles.getJSONObject(i).getInt("w");
            int h = obstacles.getJSONObject(i).getInt("h");
            System.out.println(x + " " + y + " " + h + " " + w);
            for (int j = x; j < x + w; j++){
                for (int k = y; k < y + h; k++){
                    if (j < width && j >= 0 && k < height && k >= 0){
                        this.board[j][k] = 5;
                    }
                }
            }
        }
        for (int i = 0; i < coords.length; i++){
            board[coords[i][0]][coords[i][1]] = i+1;
        }
        
        
        printBoard(this.board);
        
        
        System.out.println("Joueurs : " + config.getJSONArray("players"));

        System.out.println("Obstacles : " + config.getJSONArray("obstacles"));

        System.out.print("Taille de la grille : ");
        System.out.println(config.getInt("w") + " x " + config.getInt("h"));

        System.out.println("Votre identifiant : " + config.getString("me"));    
        
    }

    /**
     * Fonction appelée à chaque tour de jeu.
     *
     * @param prevMoves Mouvements précédents des joueurs
     * @return Mouvement à effectuer
     * @throws org.json.JSONException
     */
    public char next(JSONArray prevMoves) throws JSONException {
        String[] moves = {"", "", "", ""};
        System.out.println(prevMoves.length());
        System.out.print("IDS : ");
        
        
        for (int i = 0; i < prevMoves.length(); i++) {
            JSONObject prevMove = prevMoves.getJSONObject(i);
            System.out.print(prevMove.getString("id") + " ");
            moves[Integer.parseInt(prevMove.getString("id"))-1] = prevMove.getString("direction");
            //System.out.print(prevMove.getString("direction") + " ");
        }
        System.out.println("\nwoops " + moves.length);
        for (int i = 0; i < moves.length; i++){
            if (!moves.equals("")){
                if (moves[i].equals("u")){
                    this.coords[i][1]--;
                } else if (moves[i].equals("d")) {
                    this.coords[i][1]++;
                } else if (moves[i].equals("r")) {
                   this.coords[i][0]++;
                } else if (moves[i].equals("l")) {
                    this.coords[i][0]--;
                }
                int coordx = this.coords[i][0];
                int coordy = this.coords[i][1];
                if (coordx > 0 && coordx < width && coordy > 0 && coordy < height){
                    this.board[this.coords[i][0]][this.coords[i][1]] = i+1;
                }
            } else {
                this.coords[i][0] = -1;
                this.coords[i][1] = -1;
            }
        }
        printBoard(this.board);
        System.out.print("\n");

        direction = this.nextMove();
        System.out.println("joueur: " + this.me);
        System.out.println("Mouvement choisi : " + direction);
        return direction;
    }

    /**
     * Fonction appelée en fin de partie.
     *
     * @param winnerID ID de l'équipe gagnante
     */
    public void end(String winnerID) {
        System.out.println("Équipe gagnante : " + winnerID);
    }
    
    
    private void printBoard(int[][] board){
        System.out.println();
        for (int j = 0; j < height; j++){
            for (int i = 0; i < width; i++){
                if (board[i][j] == 0){
                    System.out.print("-");
                }else{
                    System.out.print(board[i][j]);
                }
            }
            System.out.print("\n");
        }
    }
    
    private char nextMove(){
        char res = 'u';
        boolean[] possible = {false, false, false, false}; //u l d r
        int mex = coords[this.me-1][0];
        int mey = coords[this.me-1][1];
        int possibleMoves = 0;
        
        //up (-y)
        if (isPossible(mex, mey-1)){
            possible[0] = true;
            possibleMoves++;
        }
        //left -x
        if (isPossible(mex-1, mey)) {
            possible[1] = true;
            possibleMoves++;
        }
        //down +y
        if (isPossible(mex, mey + 1)){
            possible[2] = true;
            possibleMoves++;
        }
        //right +x
        if (isPossible(mex+1, mey)){
            possible[3] = true;
            possibleMoves++;
        }
        
        int[] areas = new int[4];
        int choice = 0;
        int max = -1;
        if (possibleMoves == 0){
            System.out.println("no possible moves");
            res = this.directions[random.nextInt(directions.length)];
        } else{
            
            int area = 0;
            for (int i = 0; i < 4; i++){
                if (possible[i]){
                    switch (directions[i]) {
                        case 'u':
                            areas[i] = calculateArea(mex, mey-1);
                            break;
                        case 'l':
                            areas[i] = calculateArea(mex - 1, mey);
                            break;
                        case 'd':
                            areas[i] = calculateArea(mex, mey+ 1);
                            break;
                        case 'r':
                            areas[i] = calculateArea(mex + 1, mey);
                            break;
                    }
                    if (areas[i] > max) {
                        max = areas[i];
                    }
                }
            }
            boolean done = false;
            while (!done){
                choice = random.nextInt(directions.length);
                if (areas[choice] == max) {
                    done = true;
                }    
            }
            res = this.directions[choice];
        }
        
        return res;
    }
    
    private boolean isPossible(int x, int y){
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return false;
        } else if (board[x][y] == 0){
            boolean res = true;
            for (int i = 0; i < 4; i++){
                if (coords[i][0] >= 0){
                    if (Math.abs(x - coords[i][0]) == 1 && y == coords[i][0]) {
                        res = false;
                    } else if( Math.abs(y - coords[i][0]) == 1 && x == coords[i][0]){
                        res = false;
                    }
                }
                return res;
            }
        }
        return false;
    } 
    
    private int calculateArea(int mex, int mey){
        int area = 0;
        boolean[][] spaces = new boolean[width][height];
        int[][] positions; //nth position, x or y coord
        positions = new int[width * height][2];
        positions[0] = new int[]{mex, mey};
        int currentIndex = 0;
        
        boolean done = false;
        while (!done){
            //if up is possible
            if (isPossible(mex, mey-1)){
                area++;
                positions[area][0] = mex;
                positions[area][1] = mey - 1;
            } 
            //if left is possible
            else  if (isPossible(mex - 1, mey)){
                area++;
                positions[area][0] = mex - 1;
                positions[area][1] = mey;
            }
            // is down is possible
            else if (isPossible(mex, mey + 1)){
                area++;
                positions[area][0] = mex;
                positions[area][1] = mey + 1;
            }
            // is right possible
            else if (isPossible(mex + 1, mey)){
                area++;
                positions[area][0] = mex + 1;
                positions[area][1] = mey;
            }
            currentIndex++;
            if (currentIndex == area){
                done = true;
            }
             if (area > 20){
                 done = true;
             }
        }
        
        return area;
    }
}
