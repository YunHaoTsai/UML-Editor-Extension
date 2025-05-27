package mod.instance;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.BasicStroke;
import javax.swing.JPanel;

import Define.AreaDefine;
import Pack.DragPack;
import bgWork.handler.CanvasPanelHandler;
import mod.IFuncComponent;
import mod.ILinePainter;
import java.lang.Math;

public class DependencyLine extends JPanel implements IFuncComponent, ILinePainter {
    // 來源和目標的面板組件
    protected JPanel from;                  // 線條起點所在的面板
    protected JPanel to;                    // 線條終點所在的面板
    
    // 連接點的位置信息
    protected int fromSide;                 // 起點連接的邊（上下左右）
    protected int toSide;                   // 終點連接的邊（上下左右）
    protected Point fp = new Point(0, 0);   // 起點坐標
    protected Point tp = new Point(0, 0);   // 終點坐標
    
    // 選中狀態相關
    protected boolean isSelect = false;     // 是否被選中
    protected int selectBoxSize = 5;        // 選中時顯示的方框大小
    
    // 畫布處理器
    protected CanvasPanelHandler cph;       // 用於處理畫布相關操作

    // 構造函數
    public DependencyLine(CanvasPanelHandler cph) {
        this.setOpaque(false);            // 設置面板透明
        this.setVisible(true);            // 設置可見
        this.setMinimumSize(new Dimension(1, 1));  // 設置最小尺寸
        this.cph = cph;
    }

    // 重寫繪製組件方法
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Point fpPrime, tpPrime;
        
        // 更新連接點
        renewConnect();
        
        // 計算相對於當前面板的坐標
        fpPrime = new Point(fp.x - this.getLocation().x,
                fp.y - this.getLocation().y);
        tpPrime = new Point(tp.x - this.getLocation().x,
                tp.y - this.getLocation().y);
        
        // 設置虛線樣式
        float[] dashPattern = {10, 5};  // 虛線模式：10個像素的線段，5個像素的空隙
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
        
        // 根據選擇狀態設置顏色
        if (isSelect) {
            g2d.setColor(Color.RED);  // 選中時用紅色
        } else {
            g2d.setColor(Color.BLACK);  // 未選中時用黑色
        }
        
        // 繪製虛線
        g2d.drawLine(fpPrime.x, fpPrime.y, tpPrime.x, tpPrime.y);
        
        // 繪製箭頭
        paintArrow(g, tpPrime);
        
        // 如果被選中，繪製選中標記
        if (isSelect) {
            paintSelect(g);
        }
    }

    // 繪製箭頭
    @Override
    public void paintArrow(Graphics g, Point point) {
        int arrowSize = 10;  // 箭頭大小
        double dx = fp.x - tp.x;
        double dy = fp.y - tp.y;
        double angle = Math.atan2(dy, dx);
        
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        
        // 計算箭頭三個頂點的位置
        xPoints[0] = point.x;
        yPoints[0] = point.y;
        xPoints[1] = (int) (point.x + arrowSize * Math.cos(angle - Math.PI/6));
        yPoints[1] = (int) (point.y + arrowSize * Math.sin(angle - Math.PI/6));
        xPoints[2] = (int) (point.x + arrowSize * Math.cos(angle + Math.PI/6));
        yPoints[2] = (int) (point.y + arrowSize * Math.sin(angle + Math.PI/6));
        
        g.drawPolyline(xPoints, yPoints, 3);  // 繪製開放的箭頭
    }

    // 設置連接點
    @Override
    public void setConnect(DragPack dPack) {
        Point mfp = dPack.getFrom();
        Point mtp = dPack.getTo();
        from = (JPanel) dPack.getFromObj();
        to = (JPanel) dPack.getToObj();
        
        // 確定連接點在物件的哪一邊
        fromSide = new AreaDefine().getArea(from.getLocation(), from.getSize(), mfp);
        toSide = new AreaDefine().getArea(to.getLocation(), to.getSize(), mtp);
        
        renewConnect();
    }

    // 更新連接點位置
    private void renewConnect() {
        try {
            fp = getConnectPoint(from, fromSide);
            tp = getConnectPoint(to, toSide);
            this.reSize();
        } catch (NullPointerException e) {
            this.setVisible(false);
            cph.removeComponent(this);
        }
    }

    // 計算具體的連接點坐標
    private Point getConnectPoint(JPanel jp, int side) {
        Point temp = new Point(0, 0);
        Point jpLocation = cph.getAbsLocation(jp);
        
        // 根據不同的邊計算連接點
        if (side == new AreaDefine().TOP) {
            temp.x = (int) (jpLocation.x + jp.getSize().getWidth() / 2);
            temp.y = jpLocation.y;
        } else if (side == new AreaDefine().RIGHT) {
            temp.x = (int) (jpLocation.x + jp.getSize().getWidth());
            temp.y = (int) (jpLocation.y + jp.getSize().getHeight() / 2);
        } else if (side == new AreaDefine().LEFT) {
            temp.x = jpLocation.x;
            temp.y = (int) (jpLocation.y + jp.getSize().getHeight() / 2);
        } else if (side == new AreaDefine().BOTTOM) {
            temp.x = (int) (jpLocation.x + jp.getSize().getWidth() / 2);
            temp.y = (int) (jpLocation.y + jp.getSize().getHeight());
        } else {
            temp = null;
            System.err.println("getConnectPoint fail:" + side);
        }
        return temp;
    }

    // 重新計算大小
    @Override
    public void reSize() {
        Dimension size = new Dimension(Math.abs(fp.x - tp.x) + 10,
                Math.abs(fp.y - tp.y) + 10);
        this.setSize(size);
        this.setLocation(Math.min(fp.x, tp.x) - 5, Math.min(fp.y, tp.y) - 5);
    }

    // 繪製選中標記
    @Override
    public void paintSelect(Graphics gra) {
        gra.setColor(Color.BLACK);
        gra.fillRect(fp.x - this.getLocation().x - selectBoxSize/2,
                fp.y - this.getLocation().y - selectBoxSize/2,
                selectBoxSize, selectBoxSize);
        gra.fillRect(tp.x - this.getLocation().x - selectBoxSize/2,
                tp.y - this.getLocation().y - selectBoxSize/2,
                selectBoxSize, selectBoxSize);
    }

    // 選中狀態的 getter 和 setter
    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }

    public JPanel getFrom() {
        return from;
    }

    public JPanel getTo() {
        return to;
    }

    public int getFromSide() {
        return fromSide;
    }

    public int getToSide() {
        return toSide;
    }
} 