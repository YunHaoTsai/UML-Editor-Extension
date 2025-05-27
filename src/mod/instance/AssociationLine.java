package mod.instance;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JPanel;

import Define.AreaDefine;
import Pack.DragPack;
import bgWork.handler.CanvasPanelHandler;
import mod.IFuncComponent;
import mod.ILinePainter;
import java.lang.Math;

public class AssociationLine extends JPanel
		implements IFuncComponent, ILinePainter
{
	protected JPanel				from;
	protected int					fromSide;
	protected Point				fp				= new Point(0, 0);
	protected JPanel				to;
	protected int					toSide;
	protected Point				tp				= new Point(0, 0);
	protected boolean				isSelect		= false;
	protected int					selectBoxSize	= 5;
	protected CanvasPanelHandler	cph;

	public AssociationLine(CanvasPanelHandler cph)
	{
		this.setOpaque(false);
		this.setVisible(true);
		this.setMinimumSize(new Dimension(1, 1));
		this.cph = cph;
	}

	@Override
	public void paintComponent(Graphics g)
	{
		Point fpPrime;
		Point tpPrime;
		renewConnect();
		fpPrime = new Point(fp.x - this.getLocation().x,
				fp.y - this.getLocation().y);
		tpPrime = new Point(tp.x - this.getLocation().x,
				tp.y - this.getLocation().y);
		
		// 根據選擇狀態設置顏色
		if (isSelect) {
			g.setColor(Color.RED);  // 選中時用紅色
		} else {
			g.setColor(Color.BLACK);  // 未選中時用黑色
		}
		
		// 畫線
		g.drawLine(fpPrime.x, fpPrime.y, tpPrime.x, tpPrime.y);
		paintArrow(g, tpPrime);
		
		// 如果被選中，畫選擇框
		if (isSelect) {
			paintSelect(g);
		}
	}

	@Override
	public void reSize()
	{
		Dimension size = new Dimension(Math.abs(fp.x - tp.x) + 10,
				Math.abs(fp.y - tp.y) + 10);
		this.setSize(size);
		this.setLocation(Math.min(fp.x, tp.x) - 5, Math.min(fp.y, tp.y) - 5);
	}

	@Override
	public void paintArrow(Graphics g, Point point)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void setConnect(DragPack dPack)
	{
		Point mfp = dPack.getFrom();
		Point mtp = dPack.getTo();
		from = (JPanel) dPack.getFromObj();
		to = (JPanel) dPack.getToObj();
		fromSide = new AreaDefine().getArea(from.getLocation(), from.getSize(),
				mfp);
		toSide = new AreaDefine().getArea(to.getLocation(), to.getSize(), mtp);
		renewConnect();
	}

	void renewConnect()
	{
		try
		{
			fp = getConnectPoint(from, fromSide);
			tp = getConnectPoint(to, toSide);
			this.reSize();
		}
		catch (NullPointerException e)
		{
			this.setVisible(false);
			cph.removeComponent(this);
		}
	}

	Point getConnectPoint(JPanel jp, int side)
	{
		Point temp = new Point(0, 0);
		Point jpLocation = cph.getAbsLocation(jp);
		if (side == new AreaDefine().TOP)
		{
			temp.x = (int) (jpLocation.x + jp.getSize().getWidth() / 2);
			temp.y = jpLocation.y;
		}
		else if (side == new AreaDefine().RIGHT)
		{
			temp.x = (int) (jpLocation.x + jp.getSize().getWidth());
			temp.y = (int) (jpLocation.y + jp.getSize().getHeight() / 2);
		}
		else if (side == new AreaDefine().LEFT)
		{
			temp.x = jpLocation.x;
			temp.y = (int) (jpLocation.y + jp.getSize().getHeight() / 2);
		}
		else if (side == new AreaDefine().BOTTOM)
		{
			temp.x = (int) (jpLocation.x + jp.getSize().getWidth() / 2);
			temp.y = (int) (jpLocation.y + jp.getSize().getHeight());
		}
		else
		{
			temp = null;
			System.err.println("getConnectPoint fail:" + side);
		}
		return temp;
	}

	@Override
	public void paintSelect(Graphics gra)
	{
		Point fpPrime = new Point(fp.x - this.getLocation().x,
				fp.y - this.getLocation().y);
		Point tpPrime = new Point(tp.x - this.getLocation().x,
				tp.y - this.getLocation().y);
				
		// 畫選擇框
		gra.setColor(Color.BLUE);  // 用藍色畫選擇框
		gra.fillRect(fpPrime.x - selectBoxSize/2, fpPrime.y - selectBoxSize/2, 
				selectBoxSize, selectBoxSize);
		gra.fillRect(tpPrime.x - selectBoxSize/2, tpPrime.y - selectBoxSize/2, 
				selectBoxSize, selectBoxSize);
	}

	public boolean isSelect()
	{
		return isSelect;
	}

	public void setSelect(boolean isSelect)
	{
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
