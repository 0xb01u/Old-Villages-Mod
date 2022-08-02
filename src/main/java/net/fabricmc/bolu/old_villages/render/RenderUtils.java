package net.fabricmc.bolu.old_villages.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class RenderUtils {
	private RenderUtils() {}

	public static void prepareOpenGL(boolean beforeRender) {
		if (beforeRender) {
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.lineWidth(2f);
			GlStateManager.disableTexture();
			GlStateManager.disableCull();
			GlStateManager.enableDepthTest();
		} else {
			GlStateManager.enableCull();
			GlStateManager.enableTexture();
		}
	}

	private static void drawDot(double Ax, double Ay, double Az, Color color) {
		final int RED = color.getRed();
		final int GREEN = color.getGreen();
		final int BLUE = color.getBlue();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		bufferBuilder.begin(GL11.GL_POINT, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(Ax, Ay, Az).color(RED, GREEN, BLUE, 255).next();
		tessellator.draw();
	}

	public static void drawLine(double dx, double dy, double dz,
	                            double Ax, double Ay, double Az,
	                            double Bx, double By, double Bz,
	                            Color color) {
		final int RED = color.getRed();
		final int GREEN = color.getGreen();
		final int BLUE = color.getBlue();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		bufferBuilder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(Ax - dx, Ay - dy, Az - dz).color(RED, GREEN, BLUE, 255).next();
		bufferBuilder.vertex(Bx - dx, By - dy, Bz - dz).color(RED, GREEN, BLUE, 255).next();
		tessellator.draw();
	}

	public static void drawSphere(double radius, int sphereDensity,
	                              double dx, double dy, double dz,
	                              double Ax, double Ay, double Az,
	                              Color color, SphereDrawMode mode) {
		if (mode == SphereDrawMode.LINES) {
			sphereDensity /= 2;
		}
		final double dPhi = 2 * Math.PI / sphereDensity;
		float prevX1 = 0, prevY1 = 0, prevZ1 = 0;
		float curY = 0.02f;

		for (double phi = 0.0; phi < 2 * Math.PI; phi += dPhi) {
			double sinPhi = Math.sin(phi);
			double dTheta = Math.PI / (1 + (int) (sphereDensity * Math.abs(sinPhi)));

			float prevX = 0, prevY = 0, prevZ = 0;

			if (mode == SphereDrawMode.CIRCLE) { // Flat circle
				float curX = (float) (radius * Math.cos(phi));
				float curZ = (float) (radius * Math.sin(phi));

				if (phi != 0) {
					drawLine(dx, dy, dz, Ax + prevX1, Ay + prevY1, Az + prevZ1,
							Ax + curX, Ay + curY, Az + curZ, color);
				}
				prevX1 = curX;
				prevY1 = curY;
				prevZ1 = curZ;
				continue;
			}

			for (double theta = 0.0; theta < Math.PI + dTheta; theta += dTheta) {
				float curX = (float) (radius * sinPhi * Math.cos(theta));
				float curZ = (float) (radius * sinPhi * Math.sin(theta));
				curY = (float) (radius * Math.cos(phi));

				// mode 1 = dots; mode 2 = lines
				if (mode == SphereDrawMode.DOTS) {
					drawDot(Ax + curX - dx, Ay + curY - dy, Az + curZ - dz, color);
				} else if (mode == SphereDrawMode.LINES && theta != 0.0) {
					drawLine(dx, dy, dz, Ax + prevX, Ay + prevY, Az + prevZ,
							Ax + curX, Ay + curY, Az + curZ, color);
				}

				prevX = curX;
				prevY = curY;
				prevZ = curZ;
			}
		}
	}

	public static void drawBox(double dx, double dy, double dz, double x1, double y1, double z1,
	                           double x2, double y2, double z2, Color color) {
	}
}
