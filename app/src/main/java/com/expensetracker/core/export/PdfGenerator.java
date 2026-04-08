package com.expensetracker.core.export;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Handler;
import android.os.Looper;

import com.expensetracker.core.utils.CurrencyUtils;
import com.expensetracker.core.utils.DateUtils;
import com.expensetracker.data.local.entity.ExpenseEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PdfGenerator {

    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public PdfGenerator(Context context) {
        this.context = context;
    }

    public interface ExportCallback {
        void onSuccess(String fileName);
        void onFailure(Exception e);
    }

    public void generateReport(String type, long start, long end, List<ExpenseEntity> transactions, ExportCallback callback) {
        executor.execute(() -> {
            PdfDocument document = new PdfDocument();
            String timestamp = new SimpleDateFormat("MMM_yyyy", Locale.getDefault()).format(new Date(start));
            String fileName = "Expense_Report_" + type + "_" + timestamp + ".pdf";

            try {
                int pageWidth = 595; // A4 size in points
                int pageHeight = 842;
                int marginLeft = 40;
                int marginRight = 40;
                int marginTop = 50;
                int marginBottom = 50;

                int pageNumber = 1;
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();

                // Draw Header
                int y = marginTop;
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                paint.setTextSize(18);
                paint.setColor(Color.parseColor("#12B886")); // Primary color
                canvas.drawText("ExpenseTracker", marginLeft, y, paint);

                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                paint.setTextSize(10);
                paint.setColor(Color.GRAY);
                canvas.drawText("Premium Financial Report", marginLeft, y + 15, paint);

                y += 50;
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                paint.setTextSize(14);
                paint.setColor(Color.BLACK);
                canvas.drawText(type + " Transactions Report", marginLeft, y, paint);
                
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                paint.setTextSize(10);
                String dateRange = DateUtils.formatDateShort(start) + " - " + DateUtils.formatDateShort(end);
                canvas.drawText("Period: " + dateRange, marginLeft, y + 15, paint);

                // Summary calculations
                double totalIncome = 0;
                double totalExpense = 0;
                for (ExpenseEntity e : transactions) {
                    if ("income".equalsIgnoreCase(e.getType())) totalIncome += e.getAmount();
                    else totalExpense += e.getAmount();
                }

                y += 60;
                drawSummaryBox(canvas, marginLeft, y, pageWidth - (marginLeft + marginRight), totalIncome, totalExpense);

                // Table Header
                y += 90;
                drawTableHeader(canvas, marginLeft, y, pageWidth - (marginLeft + marginRight));
                y += 25;

                // Table Rows
                paint.setTextSize(9);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                
                for (int i = 0; i < transactions.size(); i++) {
                    ExpenseEntity tx = transactions.get(i);
                    
                    if (y > pageHeight - marginBottom - 20) {
                        drawFooter(canvas, pageNumber, pageWidth, pageHeight);
                        document.finishPage(page);
                        pageNumber++;
                        pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        y = marginTop + 20;
                        drawTableHeader(canvas, marginLeft, y, pageWidth - (marginLeft + marginRight));
                        y += 25;
                    }

                    drawTableRow(canvas, marginLeft, y, pageWidth - (marginLeft + marginRight), tx);
                    y += 25;
                }

                drawFooter(canvas, pageNumber, pageWidth, pageHeight);
                document.finishPage(page);

                // Save to file
                OutputStream os = FileHelper.getFileOutputStream(context, fileName);
                document.writeTo(os);
                document.close();
                os.close();

                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(fileName));

            } catch (Exception e) {
                document.close();
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e));
            }
        });
    }

    private void drawSummaryBox(Canvas canvas, int x, int y, int width, double income, double expense) {
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#F8F9FA"));
        canvas.drawRect(x, y, x + width, y + 60, bgPaint);

        Paint textPaint = new Paint();
        textPaint.setTextSize(10);
        textPaint.setColor(Color.GRAY);
        canvas.drawText("Total Income", x + 20, y + 25, textPaint);
        canvas.drawText("Total Expense", x + width / 3 + 20, y + 25, textPaint);
        canvas.drawText("Net Balance", x + (width / 3) * 2 + 20, y + 25, textPaint);

        textPaint.setTextSize(12);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setColor(Color.parseColor("#12B886"));
        canvas.drawText(CurrencyUtils.formatAmount(income), x + 20, y + 45, textPaint);
        
        textPaint.setColor(Color.parseColor("#FA5252"));
        canvas.drawText(CurrencyUtils.formatAmount(expense), x + width / 3 + 20, y + 45, textPaint);
        
        double balance = income - expense;
        textPaint.setColor(balance >= 0 ? Color.parseColor("#12B886") : Color.parseColor("#FA5252"));
        canvas.drawText(CurrencyUtils.formatAmount(balance), x + (width / 3) * 2 + 20, y + 45, textPaint);
    }

    private void drawTableHeader(Canvas canvas, int x, int y, int width) {
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#212529"));
        canvas.drawRect(x, y, x + width, y + 20, bgPaint);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(9);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        canvas.drawText("DATE", x + 5, y + 13, textPaint);
        canvas.drawText("CATEGORY", x + 80, y + 13, textPaint);
        canvas.drawText("DESCRIPTION", x + 180, y + 13, textPaint);
        canvas.drawText("TYPE", x + width - 120, y + 13, textPaint);
        canvas.drawText("AMOUNT", x + width - 50, y + 13, textPaint);
    }

    private void drawTableRow(Canvas canvas, int x, int y, int width, ExpenseEntity tx) {
        Paint textPaint = new Paint();
        textPaint.setTextSize(9);
        textPaint.setColor(Color.BLACK);

        canvas.drawText(DateUtils.formatDateShort(tx.getDate()), x + 5, y + 13, textPaint);
        canvas.drawText(tx.getCategory(), x + 80, y + 13, textPaint);
        
        String note = tx.getNote() != null ? tx.getNote() : "";
        if (note.length() > 30) note = note.substring(0, 27) + "...";
        canvas.drawText(note, x + 180, y + 13, textPaint);

        boolean isIncome = "income".equalsIgnoreCase(tx.getType());
        textPaint.setColor(isIncome ? Color.parseColor("#12B886") : Color.parseColor("#FA5252"));
        canvas.drawText(isIncome ? "INCOME" : "EXPENSE", x + width - 120, y + 13, textPaint);
        
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(CurrencyUtils.formatAmount(tx.getAmount()), x + width - 50, y + 13, textPaint);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#E9ECEF"));
        canvas.drawLine(x, y + 20, x + width, y + 20, linePaint);
    }

    private void drawFooter(Canvas canvas, int pageNum, int width, int height) {
        Paint paint = new Paint();
        paint.setTextSize(8);
        paint.setColor(Color.GRAY);
        String date = "Generated on: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText(date, 40, height - 30, paint);
        canvas.drawText("Page " + pageNum, width - 80, height - 30, paint);
    }
}
