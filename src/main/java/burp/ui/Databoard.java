package burp.ui;

import burp.Config;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;

import static burp.BurpExtender.stdout;

/**
 * @author LinChen && EvilChen
 */

public class Databoard extends JPanel {
    public Databoard() {
        initComponents();
    }

    /**
     * 清空数据
     */
    private void clearActionPerformed(ActionEvent e) {
        // 清空页面
        dataTabbedPane.removeAll();
        // 判断通配符Host/单一Host
        String host = hostTextField.getText();
        if(host.contains("*")){
            Map<String, Map<String,  Map<String, List<String>>>> ruleMap = Config.globalDataMap;
            Map<String, List<String>> selectHost = new HashMap<>();
            ruleMap.keySet().forEach(i -> {
                if (i.contains(host.replace("*.", ""))) {
                    Config.globalDataMap.remove(i);
                }
            });
        } else {
            Config.globalDataMap.remove(host);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        hostLabel = new JLabel();
        hostTextField = new JTextField();
        dataTabbedPane = new JTabbedPane();
        clearButton = new JButton();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {25, 0, 0, 0, 20, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 65, 20, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

        //---- hostLabel ----
        hostLabel.setText("Host:");
        add(hostLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(8, 0, 5, 5), 0, 0));
        add(hostTextField, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(8, 0, 5, 5), 0, 0));
        clearButton.setText("Clear");
        clearButton.addActionListener(this::clearActionPerformed);
        add(clearButton,  new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(8, 0, 5, 5), 0, 0));
        add(dataTabbedPane, new GridBagConstraints(1, 1, 3, 2, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(8, 0, 0, 5), 0, 0));

        setAutoMatch(hostTextField, dataTabbedPane);
    }

    /**
     * 获取Host列表
     */
    private static List<String> getHostByList(){
        List<String> hostList = new ArrayList<>();
        hostList.addAll(Config.globalDataMap.keySet());
        return hostList;
    }

    /**
     * 设置输入自动匹配
     */
    public static void setAutoMatch(JTextField textField, JTabbedPane tabbedPane) {
        final DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();

        final JComboBox hostComboBox = new JComboBox(comboBoxModel) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 0);
            }
        };

        isMatchHost = false;

        for (String host : getHostByList()) {
            comboBoxModel.addElement(host);
        }

        hostComboBox.setSelectedItem(null);

        hostComboBox.addActionListener(e -> {
            if (!isMatchHost) {
                if (hostComboBox.getSelectedItem() != null) {
                    textField.setText(hostComboBox.getSelectedItem().toString());
                    getInfoByHost(hostComboBox, tabbedPane, textField);
                }
            }
        });

        // 事件监听
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                isMatchHost = true;
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (hostComboBox.isPopupVisible()) {
                        e.setKeyCode(KeyEvent.VK_ENTER);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER
                        || e.getKeyCode() == KeyEvent.VK_UP
                        || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    e.setSource(hostComboBox);
                    hostComboBox.dispatchEvent(e);
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        textField.setText(hostComboBox.getSelectedItem().toString());
                        getInfoByHost(hostComboBox, tabbedPane, textField);
                        hostComboBox.setPopupVisible(false);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hostComboBox.setPopupVisible(false);
                }
                isMatchHost = false;
            }
        });

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateList();
            }

            private void updateList() {
                isMatchHost = true;
                comboBoxModel.removeAllElements();
                String input = textField.getText();
                if (!input.isEmpty()){
                    for (String host : getHostByList()) {
                        if (host.toLowerCase().contains(input.toLowerCase())) {
                            if (host.length() == input.length()){
                                comboBoxModel.insertElementAt(host,0);
                                comboBoxModel.setSelectedItem(host);
                            }else{
                                comboBoxModel.addElement(host);
                            }
                        }
                    }
                }
                hostComboBox.setPopupVisible(comboBoxModel.getSize() > 0);
                isMatchHost = false;
            }
        });

        textField.setLayout(new BorderLayout());
        textField.add(hostComboBox, BorderLayout.SOUTH);
    }

    private static void getInfoByHost(@NotNull JComboBox hostComboBox, JTabbedPane tabbedPane, JTextField textField) {
        if (hostComboBox.getSelectedItem() != null) {
            Map<String, Map<String,  Map<String, List<String>>>> ruleMap = Config.globalDataMap;
            Map<String, Map<String, List<String>>> selectHost = new HashMap<>();
            String host = hostComboBox.getSelectedItem().toString();
            if (host.contains("*")) {
                // 通配符数据
                Map<String, Map<String, List<String>>> finalSelectHost = selectHost;
                ruleMap.keySet().forEach(i -> {  //i=host,a=url,e=regex
                    if (i.contains(host.replace("*.", ""))) {
                        ruleMap.get(i).keySet().forEach(a -> {
                            ruleMap.get(i).get(a).keySet().forEach(e -> {
                            if (finalSelectHost.containsKey(a) && finalSelectHost.get(a).containsKey(e)) {
                                // 合并操作
                                List<String> newList = new ArrayList<>(finalSelectHost.get(a).get(e));
                                newList.addAll(ruleMap.get(i).get(a).get(e));
                                // 去重操作
                                HashSet tmpList = new HashSet(newList);
                                newList.clear();
                                newList.addAll(tmpList);
                                // 添加操作
                                Map<String,List<String>> t=finalSelectHost.get(a);
                                t.put(e,newList);
                                finalSelectHost.put(a, t);
                            } else {
                                finalSelectHost.put(a, ruleMap.get(i).get(a));
                            }
                            });
                        });
                    }
                });
            } else {
                selectHost = ruleMap.get(host);
            }

            tabbedPane.removeAll();

                for(Map.Entry<String, Map<String, List<String>>> entry1: selectHost.entrySet()){
                    for(Map.Entry<String,  List<String>> entry: entry1.getValue().entrySet()){
                        String tabName = entry.getKey();
                        int index = tabbedPane.indexOfTab(tabName);
                        try {
                            // 如果已经存在，就替换原有的组件
                            if (index >= 0) {
                                Component comp = tabbedPane.getComponentAt(index);
                                Component view = ((JScrollPane) comp).getViewport().getView();
                                JTable table = (JTable) view;
                                DefaultTableModel model = (DefaultTableModel) table.getModel();
                                Object[] row = new Object[2];
                                for (String s : entry.getValue()) {
                                    row[0] = s;
                                    row[1] = entry1.getKey();
                                    model.addRow(row);
                                }
                            } else {
                                tabbedPane.addTab(entry.getKey(), new JScrollPane(new HitRuleDataList(entry.getValue(), entry1.getKey())));
                            }
                        }finally{
                            continue;
                        }}
                }



            textField.setText(hostComboBox.getSelectedItem().toString());
        }
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel hostLabel;
    private JTextField hostTextField;
    private JTabbedPane dataTabbedPane;
    private JButton clearButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    // 是否自动匹配Host
    private static Boolean isMatchHost = false;
}

class HitRuleDataList extends JTable {
    public HitRuleDataList(List<String> list,String test){
        DefaultTableModel model = new DefaultTableModel();
        Object[][] data = new Object[list.size()][2];
        for (int x = 0; x < list.size(); x++) {
            data[x][0] = list.get(x);
            data[x][1] = test; // 添加 URL 字段
        }
        model.setDataVector(data, new Object[]{"Information","URl"});

        this.setModel(model);
        sortByUrl(); // 新增：按照 URL 字段排序
    }
    public void sortByUrl() {
        DefaultTableModel model = (DefaultTableModel) getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        // 按照 URL 字段升序排列
        sorter.setComparator(1, (o1, o2) -> {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.compareTo(s2);
        });
        setRowSorter(sorter);
    }


}
