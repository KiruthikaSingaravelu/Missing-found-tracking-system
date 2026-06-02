import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class MissingPersonTrackingSystem extends JFrame {

    /* ---------- Model ---------- */
    static class Person {
        String name;
        int age;
        String place;

        Person(String name, int age, String place) {
            this.name = name;
            this.age = age;
            this.place = place;
        }

        String toFileLine() {
            return name + "|" + age + "|" + place;
        }

        static Person fromFileLine(String line) {
            String[] p = line.split("\\|", -1);
            if (p.length != 3) return null;
            try {
                return new Person(p[0].trim(), Integer.parseInt(p[1].trim()), p[2].trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return String.format("Name: %-20s  Age: %-4d  Place: %s", name, age, place);
        }
    }

    /* ---------- Storage with file I/O ---------- */
    static class PersonStore {
        private final List<Person> list = new ArrayList<>();
        private final String fileName;

        PersonStore(String fileName) {
            this.fileName = fileName;
            load();
        }

        List<Person> getAll() { return list; }

        void add(Person p) {
            list.add(p);
            append(p);
        }

        private void load() {
            File f = new File(fileName);
            if (!f.exists()) return;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    Person p = Person.fromFileLine(line);
                    if (p != null) list.add(p);
                }
            } catch (IOException e) {
                System.err.println("Failed to load " + fileName + ": " + e.getMessage());
            }
        }

        private void append(Person p) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) {
                bw.write(p.toFileLine());
                bw.newLine();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Could not write to " + fileName + ": " + e.getMessage(),
                        "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /* ---------- UI ---------- */
    private static final Color BG        = new Color(0xF4, 0xF6, 0xFA);
    private static final Color HEADER_BG = new Color(0x1F, 0x3A, 0x5F);
    private static final Color ACCENT    = new Color(0x2E, 0x86, 0xDE);
    private static final Color SUCCESS   = new Color(0x27, 0xAE, 0x60);
    private static final Color CARD      = Color.WHITE;
    private static final Color TEXT_DARK = new Color(0x2C, 0x3E, 0x50);

    private final PersonStore missingStore = new PersonStore("missing.txt");
    private final PersonStore foundStore   = new PersonStore("found.txt");

    private JTextArea missingArea;
    private JTextArea foundArea;
    private JTextArea matchArea;

    public MissingPersonTrackingSystem() {
        setTitle("Missing Person Tracking System");
        setSize(900, 620);
        setMinimumSize(new Dimension(720, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.setBackground(BG);

        tabs.addTab("Add Missing", buildAddPanel(true));
        tabs.addTab("Add Found",   buildAddPanel(false));
        tabs.addTab("View Records", buildViewPanel());
        tabs.addTab("Match Results", buildMatchPanel());

        tabs.addChangeListener(e -> {
            int i = tabs.getSelectedIndex();
            if (i == 2) refreshViews();
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(new EmptyBorder(12, 16, 16, 16));
        wrap.setBackground(BG);
        wrap.add(tabs, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("Missing Person Tracking System");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Helping reunite families  ·  Java Swing Mini Project");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(0xCF, 0xDA, 0xE6));

        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new java.awt.GridLayout(2, 1));
        txt.add(title);
        txt.add(sub);
        header.add(txt, BorderLayout.WEST);
        return header;
    }

    private JPanel buildFooter() {
        JPanel f = new JPanel();
        f.setBackground(BG);
        JLabel l = new JLabel("Data persisted in missing.txt and found.txt");
        l.setForeground(new Color(0x7F, 0x8C, 0x8D));
        l.setFont(new Font("SansSerif", Font.ITALIC, 11));
        f.add(l);
        return f;
    }

    private JPanel buildAddPanel(boolean isMissing) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE1, 0xE5, 0xEA)),
                new EmptyBorder(28, 36, 28, 36)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;

        JLabel heading = new JLabel(isMissing ? "Report a Missing Person" : "Report a Found Person");
        heading.setFont(new Font("SansSerif", Font.BOLD, 18));
        heading.setForeground(TEXT_DARK);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        card.add(heading, g);

        g.gridwidth = 1;

        JTextField nameField  = styledField();
        JTextField ageField   = styledField();
        JTextField placeField = styledField();

        g.gridy = 1; g.gridx = 0; card.add(label("Name"), g);
        g.gridx = 1; card.add(nameField, g);

        g.gridy = 2; g.gridx = 0; card.add(label("Age"), g);
        g.gridx = 1; card.add(ageField, g);

        g.gridy = 3; g.gridx = 0; card.add(label("Place"), g);
        g.gridx = 1; card.add(placeField, g);

        JButton addBtn = new JButton(isMissing ? "Add Missing Person" : "Add Found Person");
        styleButton(addBtn, isMissing ? ACCENT : SUCCESS);

        g.gridy = 4; g.gridx = 1; g.insets = new Insets(18, 8, 8, 8);
        card.add(addBtn, g);

        addBtn.addActionListener(e -> {
            String name  = nameField.getText().trim();
            String ageS  = ageField.getText().trim();
            String place = placeField.getText().trim();

            if (name.isEmpty() || ageS.isEmpty() || place.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please fill in all fields.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int age;
            try {
                age = Integer.parseInt(ageS);
                if (age <= 0 || age > 130) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Age must be a valid number between 1 and 130.",
                        "Invalid Age", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Person p = new Person(name, age, place);
            (isMissing ? missingStore : foundStore).add(p);

            JOptionPane.showMessageDialog(this,
                    (isMissing ? "Missing" : "Found") + " person added successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            nameField.setText(""); ageField.setText(""); placeField.setText("");
            refreshViews();
        });

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(BG);
        wrap.add(card);
        return wrap;
    }

    private JPanel buildViewPanel() {
        JPanel p = new JPanel(new java.awt.GridLayout(1, 2, 12, 12));
        p.setBackground(BG);

        missingArea = makeArea();
        foundArea   = makeArea();

        p.add(wrapTitled("Missing Persons", missingArea, ACCENT));
        p.add(wrapTitled("Found Persons",   foundArea, SUCCESS));

        refreshViews();
        return p;
    }

    private JPanel buildMatchPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(BG);

        matchArea = makeArea();

        JButton match = new JButton("Run Match");
        styleButton(match, HEADER_BG);
        match.addActionListener(e -> runMatch());

        JPanel top = new JPanel();
        top.setBackground(BG);
        JLabel info = new JLabel("Matches require equal name (case-insensitive), age, and place.");
        info.setForeground(TEXT_DARK);
        top.add(info);
        top.add(match);

        p.add(top, BorderLayout.NORTH);
        p.add(wrapTitled("Match Results", matchArea, HEADER_BG), BorderLayout.CENTER);
        return p;
    }

    /* ---------- Helpers ---------- */
    private void runMatch() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Person m : missingStore.getAll()) {
            for (Person f : foundStore.getAll()) {
                if (m.name.equalsIgnoreCase(f.name)
                        && m.age == f.age
                        && m.place.equalsIgnoreCase(f.place)) {
                    count++;
                    sb.append("Match #").append(count).append('\n');
                    sb.append("  Missing -> ").append(m).append('\n');
                    sb.append("  Found   -> ").append(f).append('\n');
                    sb.append("------------------------------------------------------------\n");
                }
            }
        }
        if (count == 0) {
            matchArea.setText("No matches found.\n\nTip: add records on the first two tabs, then run match again.");
        } else {
            matchArea.setText("Total matches: " + count + "\n\n" + sb);
        }
    }

    private void refreshViews() {
        if (missingArea != null) missingArea.setText(format(missingStore.getAll()));
        if (foundArea   != null) foundArea.setText(format(foundStore.getAll()));
    }

    private String format(List<Person> list) {
        if (list.isEmpty()) return "(no records yet)";
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (Person p : list) {
            sb.append(String.format("%2d. %s%n", i++, p));
        }
        return sb.toString();
    }

    private JTextArea makeArea() {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setFont(new Font("Monospaced", Font.PLAIN, 13));
        a.setBackground(CARD);
        a.setForeground(TEXT_DARK);
        a.setMargin(new Insets(10, 12, 10, 12));
        return a;
    }

    private JPanel wrapTitled(String title, JTextArea area, Color accent) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createLineBorder(new Color(0xE1, 0xE5, 0xEA)));

        JLabel l = new JLabel("  " + title);
        l.setOpaque(true);
        l.setBackground(accent);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setBorder(new EmptyBorder(8, 8, 8, 8));

        p.add(l, BorderLayout.NORTH);
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }

    private JTextField styledField() {
        JTextField f = new JTextField(22);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCF, 0xD8, 0xDC)),
                new EmptyBorder(6, 8, 6, 8)));
        return f;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(TEXT_DARK);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        l.setPreferredSize(new Dimension(70, 24));
        return l;
    }

    private void styleButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    /* ---------- main ---------- */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MissingPersonTrackingSystem().setVisible(true));
    }
}
