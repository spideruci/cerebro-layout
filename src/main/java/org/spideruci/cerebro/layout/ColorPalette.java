package org.spideruci.cerebro.layout;

import javafx.scene.paint.Color;

import java.util.ArrayList;

/**
 * @author vpalepu
 * @since 6/1/16
 */
public class ColorPalette {

	public static String[] generatePalette(int number) {
		ArrayList<Color> colors = new ArrayList<>();
		int jump = (int) Math.ceil(360.0/number);

		if(jump < 5) {
			jump = 5;
		}

		for(int hue = 0; hue <= 360; hue += jump) {
			// var s = (colors.length % 2 === 0) ? 0.4 : 0.9;
			int saturation = 1;
			Color color = hsb(hue, saturation, 0.5);
			colors.add(color);
			if(colors.size() == number) {
				break;
			}
		}

		if(colors.size() < number) {
			ArrayList<Color> new_colors = new ArrayList<>();
			int cycle_count = (int) Math.floor(number / colors.size()) - 1;
			int residual_element_count = number % colors.size();

			for (int i = 0; i < residual_element_count; i += 1) {
				int rev_cyc_count = cycle_count + 1;
				Color color = colors.get(i);
				new_colors.add(color);
				double saturation = color.getSaturation(); // should be 1.0
				for (int j = 1; j <= 4 && j <= rev_cyc_count; j += 1) {
					saturation = saturation - 0.1;
					Color hsbColor = hsb(color, saturation);
					new_colors.add(hsbColor);
				}
			}

			for (int i = residual_element_count; i < colors.size(); i += 1) {
				int rev_cyc_count = cycle_count;
				Color color = colors.get(i);
				new_colors.add(color);
				double saturation = color.getSaturation(); // should be 1.0
				for (int j = 1; j <= 4 && j <= rev_cyc_count; j += 1) {
					saturation = saturation - 0.1;
					Color hsbColor = hsb(color, saturation);
					new_colors.add(hsbColor);
				}
			}

			colors = new_colors;
		}

		String[] colorStrings = new String[colors.size()];

		int count = 0;
		for(Color color : colors) {
			colorStrings[count] = "#" + color.toString().substring(2, 8);
			count += 1;
		}

		return colorStrings;
	}

	public static String generateSuspiciousnessColor(double power, double confidence) {
		double hue = (1f - power) * 120;
		double saturation = 1;
		double brightness = confidence;
		
		Color color = hsb(hue, saturation, brightness);

		String colorString = "#" + color.toString().substring(2, 8);

		return colorString;
	}


	private static Color hsb(double hue, double saturation, double brightness) {
		Color hsbColor = Color.hsb(hue, saturation, brightness);
		return hsbColor;
	}

	private static Color hsb(Color hsbColor, double saturation) {
		Color hsbColor2 = hsb(hsbColor.getHue(), saturation, hsbColor.getBrightness());
		return hsbColor2;
	}
}
