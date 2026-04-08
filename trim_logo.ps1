Add-Type -TypeDefinition @"
using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.Drawing.Drawing2D;

public class ImageProcessor {
    public static void ProcessLogo(string inPath, string outPath) {
        Bitmap original = new Bitmap(inPath);
        
        // Use a high-res intermediate for smoothing (1024x1024)
        int targetSize = 1024;
        Bitmap highRes = new Bitmap(targetSize, targetSize);
        Graphics gH = Graphics.FromImage(highRes);
        gH.Clear(Color.Transparent);
        gH.InterpolationMode = InterpolationMode.HighQualityBicubic;
        gH.SmoothingMode = SmoothingMode.HighQuality;
        gH.DrawImage(original, new Rectangle(0, 0, targetSize, targetSize));
        
        // Process color-based alpha masking
        Bitmap alphaMasked = new Bitmap(targetSize, targetSize);
        int tolerance = 40;
        int fadeWidth = 10; // Width of the anti-alias blend at edges
        
        Color bgHint = highRes.GetPixel(5, 5); // Background reference from corner
        
        for (int y = 0; y < targetSize; y++) {
            for (int x = 0; x < targetSize; x++) {
                Color c = highRes.GetPixel(x, y);
                int rDiff = Math.Abs(c.R - bgHint.R);
                int gDiff = Math.Abs(c.G - bgHint.G);
                int bDiff = Math.Abs(c.B - bgHint.B);
                int diff = Math.Max(rDiff, Math.Max(gDiff, bDiff));

                // Check for white background
                if (c.R > 245 && c.G > 245 && c.B > 245) {
                    alphaMasked.SetPixel(x, y, Color.Transparent);
                } else if (diff < tolerance) {
                    alphaMasked.SetPixel(x, y, Color.Transparent);
                } else if (diff < tolerance + fadeWidth) {
                    // This creates a smooth anti-aliased edge
                    float alphaFactor = (float)(diff - tolerance) / (float)fadeWidth;
                    int alpha = (int)(alphaFactor * 255);
                    alphaMasked.SetPixel(x, y, Color.FromArgb(alpha, c.R, c.G, c.B));
                } else {
                    alphaMasked.SetPixel(x, y, c);
                }
            }
        }

        // Final output (usually hdpi drawable or similar)
        // We'll save a high-res 512px output for the app to handle properly.
        int finalSize = 512;
        Bitmap output = new Bitmap(finalSize, finalSize);
        Graphics g = Graphics.FromImage(output);
        g.Clear(Color.Transparent);
        g.InterpolationMode = InterpolationMode.HighQualityBicubic;
        g.SmoothingMode = SmoothingMode.HighQuality;
        
        // Keep the same 65% centering to ensure it's not zoomed
        int iconSize = (int)(finalSize * 0.65);
        int offset = (finalSize - iconSize) / 2;
        
        g.DrawImage(alphaMasked, new Rectangle(offset, offset, iconSize, iconSize));

        output.Save(outPath, ImageFormat.Png);
        
        g.Dispose();
        gH.Dispose();
        output.Dispose();
        alphaMasked.Dispose();
        highRes.Dispose();
        original.Dispose();
    }
}
"@ -ReferencedAssemblies "System.Drawing"

[ImageProcessor]::ProcessLogo("d:\Projects\Expence Tracker\temp_new_logo.png", "d:\Projects\Expence Tracker\app\src\main\res\drawable\ic_app_logo_foreground.png")
