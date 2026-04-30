import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import processing.core.PApplet;

public class Util {
    public static void saveFrame(PApplet p, String imgFileName) {
        int saveIdx = findHighestIndex(p, imgFileName) + 1;
        String idx = String.format("%04d", saveIdx);
        p.saveFrame("output/" + imgFileName + "-" + idx + ".png");
        System.out.println("Saved: " + imgFileName + "-" + idx + ".png");
        saveIdx++;
    }

    private static int findHighestIndex(PApplet p, String base) {
        File dir = new File(p.sketchPath("output"));
        File[] files = dir.listFiles();
        if (files == null) return 0;

        Pattern pattern = Pattern.compile(Pattern.quote(base) + "-(\\d{4})\\.png");
        int max = 0;
        for (File file : files) {
            Matcher matcher = pattern.matcher(file.getName());
            if (!matcher.matches()) continue;
            int value = Integer.parseInt(matcher.group(1));
            if (value > max) max = value;
        }
        return max;
    }
}
