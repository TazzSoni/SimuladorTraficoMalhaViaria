package Controller;

import Controller.Observer.Observer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import Model.AbstractFactory.AbstractCell;
import Model.Carro;
import Model.MoveType;
import utils.MatrixManager;
import Model.Estrada;

public class TelaController implements Controller {

    private static TelaController instance;
    private final MatrixManager matrixManager = MatrixManager.getInstance();
    private List<Carro> carros = new ArrayList();
    private AbstractCell[][] cells;
    private List<Observer> observers = new ArrayList();
    private String threadMethodType;
    private final String filename = "malhas/malha-exemplo-3.txt";
    private boolean stopped = false;

    private TelaController() {
        try {
            this.matrixManager.print(filename);
            this.matrixManager.loadEntriesAndExits();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

        this.initRoadCells();
    }

    public static TelaController getInstance() {
        if (instance == null) {
            instance = new TelaController();
        }

        return instance;
    }

    public void attach(Observer obs) {
        this.observers.add(obs);
    }

    public void detach(Observer obs) {
        this.observers.remove(obs);
    }

    public void changeThreadMethodType(String opt) {
        this.threadMethodType = opt;
        try {
            matrixManager.changeMethodType(filename, threadMethodType);
        } catch (IOException ex){
            ex.printStackTrace();
        }
        notifyStartButton(true);
    }

    public String getThreadMethodType(){
        return threadMethodType;
    }

    public void start() {
        notifyStartButton(false);
        notifyEndButton(true);
        Carro newCarro = new Carro(this);

        Integer[] pos = getFirstCell();
        newCarro.setFirstPosition(pos[0], pos[1]);

        this.carros.add(newCarro);
        notifyCounter();
        this.updateRoadView(newCarro);
        newCarro.start();
    }

    @Override
    public void stop() {
        this.stopped = true;
        notifyStartButton(true);
        notifyEndButton(false);
    }

    public MatrixManager getMatrixManager() {
        return this.matrixManager;
    }

    private void initRoadCells() {
        this.cells = matrixManager.getMatriz();
        List<Integer> stopCells = BaseRoad.getStopCells();

        int row = this.matrixManager.getRows();
        int col = this.matrixManager.getCols();

        for (int i = 0; i < row; ++i) {
            for (int j = 0; j < col; ++j) {
                if (setLastCell(new Integer[]{i, j})) {
                    this.cells[i][j].setLastCell(true);
                }

                if (stopCells.contains(cells[i][j].getMoveType())) {
                    cells[i][j].setStopCell(true);
                }
            }
        }
    }

    private boolean setLastCell(Integer[] array) {
        for (Integer[] aValue :
                this.matrixManager.getExits()) {
            if (Arrays.equals(aValue, array)) {
                return true;
            }
        }
        return false;
    }

    public void setStopped(boolean status){
        this.stopped = status;
    }

    public boolean isStopped(){
        return stopped;
    }

    public void updateCarroContador(Carro c){
        this.carros.remove(c);
        notifyCounter();
    }

    public Icon renderCell(int row, int col) {
        return this.cells[row][col].getIcon();
    }

    private Integer[] getFirstCell() {
        Collections.shuffle(this.matrixManager.getEntries());
        return this.matrixManager.getEntries().get(0);
    }

    public int getCars(){
        return this.carros.size();
    }

    public void updateRoadView(Carro c) {
        int i = c.getRow();
        int j = c.getColumn();

        int moveType = this.matrixManager.getValueAtPosition(i, j);
        if(moveType >= 5){
            this.cells[i][j].setIcon(new ImageIcon(MoveType.convertMoveType(moveType)));
        }else {
            this.cells[i][j].setIcon(new ImageIcon(MoveType.getMoveType(moveType)));
        }

        notifyUpdate();
    }

    public void notifyUpdate() {
        for (Observer observer : observers) {
            observer.updateCarroPosicao();
        }
    }

    public void notifyStartButton(boolean status) {
        for (Observer observer : observers) {
            observer.changeStartButtonStatus(status);
        }
    }

    public void notifyEndButton(boolean status) {
        for (Observer observer : observers) {
            observer.changeEndButtonStatus(status);
        }
    }

    public void notifyCounter(){
        for (Observer observer : observers) {
            observer.changeCounter(this.getCars());
        }
    }

    public AbstractCell getCellAtPosition(int row, int col) {
        return cells[row][col];
    }
}

 
