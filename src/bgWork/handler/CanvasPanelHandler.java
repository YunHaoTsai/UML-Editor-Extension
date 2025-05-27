package bgWork.handler;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import Define.AreaDefine;
import Listener.CPHActionListener;
import Pack.DragPack;
import Pack.SendText;
import bgWork.InitProcess;
import mod.instance.AssociationLine;
import mod.instance.BasicClass;
import mod.instance.CompositionLine;
import mod.instance.GeneralizationLine;
import mod.instance.GroupContainer;
import mod.instance.UseCase;
import mod.instance.DependencyLine;

public class CanvasPanelHandler extends PanelHandler
{
	Vector <JPanel>	members		= new Vector <>();
	Vector <JPanel>	selectComp	= new Vector <>();
	int				boundShift	= 10;
	private JPanel lastHighlightedPort = null;

	public CanvasPanelHandler(JPanel Container, InitProcess process)
	{
		super(Container, process);
		boundDistance = 10;
		initContextPanel();
		Container.add(this.contextPanel);
	}

	@Override
	void initContextPanel()
	{
		JPanel fphContextPanel = core.getFuncPanelHandler().getContectPanel();
		contextPanel = new JPanel();
		contextPanel.setBounds(
				fphContextPanel.getLocation().x
						+ fphContextPanel.getSize().width + boundShift,
				fphContextPanel.getLocation().y, 800, 600);
		contextPanel.setLayout(null);
		contextPanel.setVisible(true);
		contextPanel.setBackground(Color.WHITE);
		contextPanel.setBorder(new LineBorder(Color.BLACK));
		contextPanel.addMouseListener(new CPHActionListener(this));
	}

	@Override
	public void ActionPerformed(MouseEvent e)
	{
		switch (core.getCurrentFuncIndex())
		{
			case 0:
				selectByClick(e);
				break;
			case 1:
			case 2:
			case 3:
			case 6:
				break;
			case 4:
			case 5:
				addObject(core.getCurrentFunc(), e.getPoint());
				break;
			default:
				break;
		}
		repaintComp();
	}

	public void ActionPerformed(DragPack dp)
	{
		switch (core.getCurrentFuncIndex())
		{
			case 0:
				selectByDrag(dp);
				break;
			case 1:
			case 2:
			case 3:
			case 6:
				addLine(core.getCurrentFunc(), dp);
				break;
			case 4:
			case 5:
				break;
			default:
				break;
		}
		repaintComp();
	}

	public void repaintComp()
	{
		for (int i = 0; i < members.size(); i ++)
		{
			members.elementAt(i).repaint();
		}
		contextPanel.updateUI();
	}

	private int isPort(JPanel jp, Point point) {
		Point jpLocation = jp.getLocation();
		int width = jp.getSize().width;
		int height = jp.getSize().height;
		int portSize = 10; // port 的判定範圍大小

		// 將點擊座標轉換為相對於元件的座標
		Point relativePoint = new Point(point.x - jpLocation.x, point.y - jpLocation.y);

		// 檢查四個 port 位置
		// Top port (3)
		if (Math.abs(relativePoint.x - width/2) <= portSize/2 &&
			Math.abs(relativePoint.y) <= portSize/2) {
			return new AreaDefine().TOP;  // TOP = 3
		}
		// Right port (2)
		if (Math.abs(relativePoint.x - width) <= portSize/2 &&
			Math.abs(relativePoint.y - height/2) <= portSize/2) {
			return new AreaDefine().RIGHT;  // RIGHT = 2
		}
		// Left port (1)
		if (Math.abs(relativePoint.x) <= portSize/2 &&
			Math.abs(relativePoint.y - height/2) <= portSize/2) {
			return new AreaDefine().LEFT;  // LEFT = 1
		}
		// Bottom port (0)
		if (Math.abs(relativePoint.x - width/2) <= portSize/2 &&
			Math.abs(relativePoint.y - height) <= portSize/2) {
			return new AreaDefine().BOTTOM;  // BOTTOM = 0
		}

		return -1;  // 不是 port
	}

	void selectByClick(MouseEvent e)
	{
		boolean isSelect = false;
		selectComp = new Vector <>();

		// 先檢查是否點擊在 port 上
		boolean clickedOnPort = false;
		JPanel clickedPort = null;
		int clickedPortSide = -1;

		// 檢查所有 BasicClass 是否被點擊在 port 上
		for (int i = members.size() - 1; i >= 0; i--) {
			JPanel member = members.elementAt(i);
			if (core.isClass(member) != -1) {
				if (isInside(member, e.getPoint())) {
					// 如果點擊在 class 內部，檢查是否點擊在 port 上
					int portSide = isPort(member, e.getPoint());
					if (portSide != -1) {
						clickedPort = member;
						clickedPortSide = portSide;
						clickedOnPort = true;
						break;
					}
				}
			}
		}

		// 如果點擊在 port 上，尋找與此 port 相連的線
		if (clickedOnPort) {
			// 取消所有元件的選取狀態
			for (JPanel member : members) {
				setSelectAllType(member, false);
			}

			// 尋找與此 port 相連的線
			boolean foundConnectedLine = false;
			for (int i = 0; i < members.size(); i++) {
				JPanel member = members.elementAt(i);
				if (core.isAssociationLine(member) || 
					core.isCompositionLine(member) || 
					core.isGeneralizationLine(member) || 
					core.isDependencyLine(member)) {
					
					// 檢查線的兩端是否連接到此 port 的具體位置
					boolean isConnected = isLineConnectedToPortSide(member, clickedPort, clickedPortSide);
					if (isConnected) {
						setSelectAllType(member, true);
						selectComp.add(member);
						foundConnectedLine = true;
					}
				}
			}
			
			// 只有在找到連接的線時才設置 lastHighlightedPort
			if (foundConnectedLine) {
				lastHighlightedPort = clickedPort;
			} else {
				lastHighlightedPort = null;
				// 如果點擊在 port 上但沒有連接的線，視為普通點擊
				setSelectAllType(clickedPort, true);
				selectComp.add(clickedPort);
				isSelect = true;
			}
		} else {
			// 取消所有元件的選取狀態
			for (JPanel member : members) {
				setSelectAllType(member, false);
			}
			lastHighlightedPort = null;

			// 一般點擊處理邏輯
			for (int i = members.size() - 1; i >= 0; i--) {
				JPanel member = members.elementAt(i);
				if (isInside(member, e.getPoint()) && !isSelect) {
					switch (core.isFuncComponent(member)) {
						case 0:  // BasicClass
						case 1:  // UseCase
							setSelectAllType(member, true);
							selectComp.add(member);
							isSelect = true;
							break;
						case 5:  // GroupContainer
							Point p = e.getPoint();
							p.x -= member.getLocation().x;
							p.y -= member.getLocation().y;
							if (groupIsSelect((GroupContainer) member, p)) {
								setSelectAllType(member, true);
								selectComp.add(member);
								isSelect = true;
							}
							break;
						default:
							break;
					}
					if (isSelect) break;  // 如果已經選中物件，就不再繼續檢查
				}
			}
		}
		repaintComp();
	}

	private boolean isLineConnectedToPortSide(JPanel line, JPanel port, int portSide) {
		if (core.isAssociationLine(line)) {
			AssociationLine aLine = (AssociationLine) line;
			return (aLine.getFrom() == port && aLine.getFromSide() == portSide) || 
				   (aLine.getTo() == port && aLine.getToSide() == portSide);
		} else if (core.isCompositionLine(line)) {
			CompositionLine cLine = (CompositionLine) line;
			return (cLine.getFrom() == port && cLine.getFromSide() == portSide) || 
				   (cLine.getTo() == port && cLine.getToSide() == portSide);
		} else if (core.isGeneralizationLine(line)) {
			GeneralizationLine gLine = (GeneralizationLine) line;
			return (gLine.getFrom() == port && gLine.getFromSide() == portSide) || 
				   (gLine.getTo() == port && gLine.getToSide() == portSide);
		} else if (core.isDependencyLine(line)) {
			DependencyLine dLine = (DependencyLine) line;
			return (dLine.getFrom() == port && dLine.getFromSide() == portSide) || 
				   (dLine.getTo() == port && dLine.getToSide() == portSide);
		}
		return false;
	}

	boolean groupIsSelect(GroupContainer container, Point point)
	{
		for (int i = 0; i < container.getComponentCount(); i ++)
		{
			if (core.isGroupContainer(container.getComponent(i)))
			{
				point.x -= container.getComponent(i).getLocation().x;
				point.y -= container.getComponent(i).getLocation().y;
				if (groupIsSelect((GroupContainer) container.getComponent(i),
						point) == true)
				{
					return true;
				}
				else
				{
					point.x += container.getComponent(i).getLocation().x;
					point.y += container.getComponent(i).getLocation().y;
				}
			}
			else if (core.isJPanel(container.getComponent(i)))
			{
				if (isInside((JPanel) container.getComponent(i), point))
				{
					return true;
				}
			}
		}
		return false;
	}

	boolean selectByDrag(DragPack dp)
	{
		if (isInSelect(dp.getFrom()) == true)
		{
			// dragging components
			Dimension shift = new Dimension(dp.getTo().x - dp.getFrom().x,
					dp.getTo().y - dp.getFrom().y);
			for (int i = 0; i < selectComp.size(); i ++)
			{
				JPanel jp = selectComp.elementAt(i);
				// 只移動非線條的元件
				if (!core.isAssociationLine(jp) && 
					!core.isCompositionLine(jp) && 
					!core.isGeneralizationLine(jp) && 
					!core.isDependencyLine(jp)) {
					jp.setLocation(jp.getLocation().x + shift.width,
							jp.getLocation().y + shift.height);
					if (jp.getLocation().x < 0)
					{
						jp.setLocation(0, jp.getLocation().y);
					}
					if (jp.getLocation().y < 0)
					{
						jp.setLocation(jp.getLocation().x, 0);
					}
				}
			}
			// 更新所有線條
			for (int i = 0; i < members.size(); i++) {
				JPanel member = members.elementAt(i);
				if (core.isAssociationLine(member) || 
					core.isCompositionLine(member) || 
					core.isGeneralizationLine(member) || 
					core.isDependencyLine(member)) {
					member.repaint();
				}
			}
			return true;
		}
		if (dp.getFrom().x > dp.getTo().x && dp.getFrom().y > dp.getTo().y)
		{
			// drag right down from to left up
			groupInversSelect(dp);
			return true;
		}
		else if (dp.getFrom().x < dp.getTo().x && dp.getFrom().y < dp.getTo().y)
		{
			// drag from left up to right down
			groupSelect(dp);
			return true;
		}
		return false;
	}

	public void setGroup()
	{
		if (selectComp.size() > 1)
		{
			GroupContainer gContainer = new GroupContainer(core);
			gContainer.setVisible(true);
			Point p1 = new Point(selectComp.elementAt(0).getLocation().x,
					selectComp.elementAt(0).getLocation().y);
			Point p2 = new Point(selectComp.elementAt(0).getLocation().x,
					selectComp.elementAt(0).getLocation().y);
			Point testP;
			for (int i = 0; i < selectComp.size(); i ++)
			{
				testP = selectComp.elementAt(i).getLocation();
				if (p1.x > testP.x)
				{
					p1.x = testP.x;
				}
				if (p1.y > testP.y)
				{
					p1.y = testP.y;
				}
				if (p2.x < testP.x + selectComp.elementAt(i).getSize().width)
				{
					p2.x = testP.x + selectComp.elementAt(i).getSize().width;
				}
				if (p2.y < testP.y + selectComp.elementAt(i).getSize().height)
				{
					p2.y = testP.y + selectComp.elementAt(i).getSize().height;
				}
			}
			p1.x --;
			p1.y --;
			gContainer.setLocation(p1);
			gContainer.setSize(p2.x - p1.x + 2, p2.y - p1.y + 2);
			for (int i = 0; i < selectComp.size(); i ++)
			{
				JPanel temp = selectComp.elementAt(i);
				removeComponent(temp);
				gContainer.add(temp, i);
				temp.setLocation(temp.getLocation().x - p1.x,
						temp.getLocation().y - p1.y);
			}
			addComponent(gContainer);
			selectComp = new Vector <>();
			selectComp.add(gContainer);
			repaintComp();
		}
	}

	public void setUngroup()
	{
		int size = selectComp.size();
		for (int i = 0; i < size; i ++)
		{
			if (core.isGroupContainer(selectComp.elementAt(i)))
			{
				GroupContainer gContainer = (GroupContainer) selectComp
						.elementAt(i);
				Component temp;
				int j = 0;
				while (gContainer.getComponentCount() > 0)
				{
					temp = gContainer.getComponent(0);
					temp.setLocation(
							temp.getLocation().x + gContainer.getLocation().x,
							temp.getLocation().y + gContainer.getLocation().y);
					addComponent((JPanel) temp, j);
					selectComp.add((JPanel) temp);
					gContainer.remove(temp);
					j ++;
				}
				removeComponent(gContainer);
				selectComp.remove(gContainer);
			}
			repaintComp();
		}
	}

	void groupSelect(DragPack dp)
	{
		JPanel jp = new JPanel();
		jp.setLocation(dp.getFrom());
		jp.setSize(Math.abs(dp.getTo().x - dp.getFrom().x),
				Math.abs(dp.getTo().y - dp.getFrom().x));
		selectComp = new Vector <>();
		for (int i = 0; i < members.size(); i ++)
		{
			if (isInside(jp, members.elementAt(i)) == true)
			{
				selectComp.add(members.elementAt(i));
				setSelectAllType(members.elementAt(i), true);
			}
			else
			{
				setSelectAllType(members.elementAt(i), false);
			}
		}
	}

	void groupInversSelect(DragPack dp)
	{
		JPanel jp = new JPanel();
		jp.setLocation(dp.getTo());
		jp.setSize(Math.abs(dp.getTo().x - dp.getFrom().x),
				Math.abs(dp.getTo().y - dp.getFrom().x));
		selectComp = new Vector <>();
		for (int i = 0; i < members.size(); i ++)
		{
			if (isInside(jp, members.elementAt(i)) == false)
			{
				selectComp.add(members.elementAt(i));
				setSelectAllType(members.elementAt(i), true);
			}
			else
			{
				setSelectAllType(members.elementAt(i), false);
			}
		}
	}

	boolean isInSelect(Point point)
	{
		for (int i = 0; i < selectComp.size(); i ++)
		{
			if (isInside(selectComp.elementAt(i), point) == true)
			{
				return true;
			}
		}
		return false;
	}

	void addLine(JPanel funcObj, DragPack dPack)
	{
		JPanel fromObj = null;
		JPanel toObj = null;
		
		// 尋找起點和終點
		for (int i = 0; i < members.size(); i ++)
		{
			JPanel member = members.elementAt(i);
			// 只檢查 BasicClass 和 UseCase
			if (core.isClass(member) != -1) {
				if (isInside(member, dPack.getFrom())) {
					fromObj = member;
				}
				if (isInside(member, dPack.getTo())) {
					toObj = member;
				}
			}
		}
		
		// 設置連接點
		dPack.setFromObj(fromObj);
		dPack.setToObj(toObj);
		
		if (fromObj == null || toObj == null || fromObj == toObj)
		{
			return;
		}

		switch (core.isLine(funcObj))
		{
			case 0:
				((AssociationLine) funcObj).setConnect(dPack);
				break;
			case 1:
				((CompositionLine) funcObj).setConnect(dPack);
				break;
			case 2:
				((GeneralizationLine) funcObj).setConnect(dPack);
				break;
			case 3:
				((DependencyLine) funcObj).setConnect(dPack);
				break;
			default:
				return;
		}
		
		// 將線條添加到最前面
		contextPanel.add(funcObj, 0);
		members.insertElementAt(funcObj, 0);
	}

	void addObject(JPanel funcObj, Point point)
	{
		if (members.size() > 0)
		{
			members.insertElementAt(funcObj, 0);
		}
		else
		{
			members.add(funcObj);
		}
		members.elementAt(0).setLocation(point);
		members.elementAt(0).setVisible(true);
		contextPanel.add(members.elementAt(0), 0);
	}

	public boolean isInside(JPanel container, Point point)
	{
		Point cLocat = container.getLocation();
		Dimension cSize = container.getSize();
		if (point.x >= cLocat.x && point.y >= cLocat.y)
		{
			if (point.x <= cLocat.x + cSize.width
					&& point.y <= cLocat.y + cSize.height)
			{
				return true;
			}
		}
		return false;
	}

	public boolean isInside(JPanel container, JPanel test)
	{
		Point cLocat = container.getLocation();
		Dimension cSize = container.getSize();
		Point tLocat = test.getLocation();
		Dimension tSize = test.getSize();
		if (cLocat.x <= tLocat.x && cLocat.y <= tLocat.y)
		{
			if (cLocat.x + cSize.width >= tLocat.x + tSize.width
					&& cLocat.y + cSize.height >= tLocat.y + tSize.height)
			{
				return true;
			}
		}
		return false;
	}

	public JPanel getSingleSelectJP()
	{
		if (selectComp.size() == 1)
		{
			return selectComp.elementAt(0);
		}
		return null;
	}

	public void setContext(SendText tr)
	{
		try
		{
			switch (core.isClass(tr.getDest()))
			{
				case 0:
					((BasicClass) tr.getDest()).setText(tr.getText());
					break;
				case 1:
					((UseCase) tr.getDest()).setText(tr.getText());
					break;
				default:
					break;
			}
		}
		catch (Exception e)
		{
			System.err.println("CPH error");
		}
	}

	void addComponent(JPanel comp)
	{
		contextPanel.add(comp, 0);
		members.insertElementAt(comp, 0);
	}

	void addComponent(JPanel comp, int index)
	{
		contextPanel.add(comp, index);
		members.insertElementAt(comp, index);
	}

	public void removeComponent(JPanel comp)
	{
		contextPanel.remove(comp);
		members.remove(comp);
	}

	void setSelectAllType(Object obj, boolean isSelect)
	{
		int type = core.isFuncComponent(obj);
		try {
			switch (type)
			{
				case 0:
					((BasicClass) obj).setSelect(isSelect);
					break;
				case 1:
					((UseCase) obj).setSelect(isSelect);
					break;
				case 2:
					((AssociationLine) obj).setSelect(isSelect);
					break;
				case 3:
					((CompositionLine) obj).setSelect(isSelect);
					break;
				case 4:
					((GeneralizationLine) obj).setSelect(isSelect);
					break;
				case 5:
					((GroupContainer) obj).setSelect(isSelect);
					break;
				case 6:
					((DependencyLine) obj).setSelect(isSelect);
					break;
				default:
					break;
			}
		} catch (Exception e) {
			System.err.println("Error in setSelectAllType: " + e.getMessage());
		}
	}

	public Point getAbsLocation(Container panel)
	{
		Point location = panel.getLocation();
		while (panel.getParent() != contextPanel)
		{
			panel = panel.getParent();
			location.x += panel.getLocation().x;
			location.y += panel.getLocation().y;
		}
		return location;
	}
}
