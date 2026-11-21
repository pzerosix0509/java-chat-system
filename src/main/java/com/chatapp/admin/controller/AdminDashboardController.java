package com.chatapp.admin.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.chatapp.admin.component.ChartPane;
import com.chatapp.admin.component.MetricCard;
import com.chatapp.admin.dto.AdminProfile;
import com.chatapp.admin.navigation.AdminRoute;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AdminDashboardController {

    private static final List<Integer> REGISTRATION_DATA =
        List.of(
            62, 74, 58, 88, 97, 105,
            121, 113, 136, 149, 158, 172
        );

    private static final List<Integer> ACTIVE_USER_DATA =
        List.of(
            1028, 1094, 1132, 1198, 1255, 1320,
            1388, 1410, 1492, 1558, 1676, 1842
        );

    @FXML
    private Label heroGreetingLabel;

    @FXML
    private Label heroDateLabel;

    @FXML
    private MetricCard totalUsersCard;

    @FXML
    private MetricCard activeUsersCard;

    @FXML
    private MetricCard groupsCard;

    @FXML
    private MetricCard pendingReportsCard;

    @FXML
    private ChartPane registrationChartPane;

    @FXML
    private ChartPane activityChartPane;

    @FXML
    private VBox recentUsersList;

    @FXML
    private VBox newUsersList;

    private Consumer<AdminRoute> navigationHandler = route -> {
    };

    private String adminDisplayName = "Admin";

    @FXML
    private void initialize() {
        configureHero();
        configureMetrics();
        configureRegistrationChart();
        configureActivityChart();
        populateUserList(recentUsersList, recentUsers());
        populateUserList(newUsersList, newUsers());
    }

    public void setAdminProfile(AdminProfile profile) {
        adminDisplayName = profile.displayName();
        configureHero();
    }

    public void setNavigationHandler(
        Consumer<AdminRoute> navigationHandler
    ) {
        this.navigationHandler = navigationHandler != null
            ? navigationHandler
            : route -> {
            };
    }

    @FXML
    private void showAllUsers() {
        navigationHandler.accept(AdminRoute.USERS);
    }

    @FXML
    private void showAllNewUsers() {
        navigationHandler.accept(AdminRoute.NEW_USERS);
    }

    private void configureHero() {
        int hour = LocalTime.now().getHour();
        String greeting;

        if (hour < 12) {
            greeting = "Chào buổi sáng";
        } else if (hour < 18) {
            greeting = "Chào buổi chiều";
        } else {
            greeting = "Chào buổi tối";
        }

        heroGreetingLabel.setText(
            greeting + ", " + adminDisplayName + "!"
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
            "d 'tháng' M, uuuu",
            Locale.forLanguageTag("vi-VN")
        );
        heroDateLabel.setText(LocalDate.now().format(formatter));
    }

    private void configureMetrics() {
        totalUsersCard.setMetric(
            "Tổng người dùng",
            "2.486",
            "+12,4% tháng này",
            "fas-users",
            false
        );
        activeUsersCard.setMetric(
            "Đang hoạt động",
            "1.842",
            "74,1% tổng người dùng",
            "fas-chart-line",
            false
        );
        groupsCard.setMetric(
            "Nhóm chat",
            "326",
            "+18 nhóm mới",
            "fas-comments",
            false
        );
        pendingReportsCard.setMetric(
            "Báo cáo chờ xử lý",
            "6",
            "Cần xem xét",
            "fas-shield-alt",
            true
        );
    }

    private void configureRegistrationChart() {
        CategoryAxis xAxis = createMonthAxis();
        NumberAxis yAxis = new NumberAxis(0, 200, 50);
        configureNumberAxis(yAxis);

        BarChart<String, Number> chart = new BarChart<>(
            xAxis,
            yAxis
        );
        chart.getStyleClass().addAll(
            "dashboard-chart",
            "registration-chart"
        );
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setBarGap(4);
        chart.setCategoryGap(22);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);

        XYChart.Series<String, Number> series =
            createSeries(REGISTRATION_DATA);
        chart.getData().add(series);

        registrationChartPane.setHeader(
            "Người dùng đăng ký mới",
            "Số tài khoản mới theo từng tháng",
            "BIỂU ĐỒ CỘT"
        );
        registrationChartPane.setChart(chart);
        registrationChartPane.setStats(
            new ChartPane.Stat("Tổng năm", "1.333"),
            new ChartPane.Stat("Cao nhất", "172 · T12"),
            new ChartPane.Stat("Trung bình", "111 / tháng")
        );
        registrationChartPane.setInsight(
            "+8,9%",
            "Sau nhịp giảm ở tháng 3, lượng đăng ký "
                + "phục hồi và tăng ổn định. Tháng 12 cao "
                + "gấp 2,8 lần tháng 1.",
            false
        );
    }

    private void configureActivityChart() {
        CategoryAxis xAxis = createMonthAxis();
        NumberAxis yAxis = new NumberAxis(0, 2000, 500);
        configureNumberAxis(yAxis);

        LineChart<String, Number> chart = new LineChart<>(
            xAxis,
            yAxis
        );
        chart.getStyleClass().addAll(
            "dashboard-chart",
            "activity-chart"
        );
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setAnimated(false);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);

        XYChart.Series<String, Number> series =
            createSeries(ACTIVE_USER_DATA);
        chart.getData().add(series);

        activityChartPane.setHeader(
            "Tăng trưởng người dùng hoạt động",
            "Người có mở ứng dụng theo từng tháng",
            "BIỂU ĐỒ ĐƯỜNG"
        );
        activityChartPane.setChart(chart);
        activityChartPane.setStats(
            new ChartPane.Stat("Hiện tại", "1.842"),
            new ChartPane.Stat("Cao nhất", "1.842 · T12"),
            new ChartPane.Stat("Tăng cả năm", "+79,2%")
        );
        activityChartPane.setInsight(
            "+9,9%",
            "Lượng người hoạt động tăng liên tục trong 5 "
                + "tháng cuối năm. Tháng 12 cao hơn tháng 1 "
                + "khoảng 79,2%.",
            true
        );
    }

    private CategoryAxis createMonthAxis() {
        CategoryAxis axis = new CategoryAxis();
        axis.setTickLabelGap(4);
        axis.setAnimated(false);
        return axis;
    }

    private void configureNumberAxis(NumberAxis axis) {
        axis.setForceZeroInRange(true);
        axis.setMinorTickVisible(false);
        axis.setAnimated(false);
    }

    private XYChart.Series<String, Number> createSeries(
        List<Integer> values
    ) {
        XYChart.Series<String, Number> series =
            new XYChart.Series<>();

        for (int index = 0; index < values.size(); index++) {
            series.getData().add(
                new XYChart.Data<>(
                    "T" + (index + 1),
                    values.get(index)
                )
            );
        }
        return series;
    }

    private void populateUserList(
        VBox container,
        List<DashboardUser> users
    ) {
        container.getChildren().clear();

        for (int index = 0; index < users.size(); index++) {
            container.getChildren().add(
                createUserRow(users.get(index), index)
            );
        }
    }

    private HBox createUserRow(
        DashboardUser user,
        int colorIndex
    ) {
        Label avatar = new Label(user.initials());
        avatar.getStyleClass().addAll(
            "dashboard-user-avatar",
            "dashboard-avatar-" + (colorIndex % 4)
        );

        Label name = new Label(user.name());
        name.getStyleClass().add("dashboard-user-name");

        Label username = new Label("@" + user.username());
        username.getStyleClass().add("dashboard-user-username");

        VBox identity = new VBox(3, name, username);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label meta = new Label(user.meta());
        meta.getStyleClass().add("dashboard-user-meta");

        Label status = new Label(user.status());
        status.getStyleClass().addAll(
            "dashboard-user-status",
            user.statusClass()
        );

        VBox summary = new VBox(5, meta, status);
        summary.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(
            10,
            avatar,
            identity,
            spacer,
            summary
        );
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("dashboard-user-row");
        return row;
    }

    private List<DashboardUser> recentUsers() {
        return List.of(
            new DashboardUser(
                "TP", "Trương Duy Phú", "truongduyphu",
                "5 phút trước", "Đang hoạt động",
                "dashboard-status-active"
            ),
            new DashboardUser(
                "TV", "Tuấn Văn", "tuanvan",
                "12 phút trước", "Đang hoạt động",
                "dashboard-status-active"
            ),
            new DashboardUser(
                "HH", "Hữu Hiệp", "huuhiep",
                "35 phút trước", "Đang hoạt động",
                "dashboard-status-active"
            ),
            new DashboardUser(
                "DH", "Duy Hiếu", "duyhieu",
                "1 giờ trước", "Đã khóa",
                "dashboard-status-locked"
            )
        );
    }

    private List<DashboardUser> newUsers() {
        return List.of(
            new DashboardUser(
                "TA", "Tuấn Anh", "tuananh",
                "17/11/2026 · 10:42", "Đang hoạt động",
                "dashboard-status-active"
            ),
            new DashboardUser(
                "DP", "Đỗ Phúc", "dophuc",
                "17/11/2026 · 09:15", "Đang hoạt động",
                "dashboard-status-active"
            ),
            new DashboardUser(
                "LL", "Lew.Lew", "lewlew",
                "16/11/2026 · 22:05", "Đã khóa",
                "dashboard-status-locked"
            ),
            new DashboardUser(
                "DH", "Duy Hiếu", "duyhieu",
                "15/11/2026 · 18:37", "Đang hoạt động",
                "dashboard-status-active"
            )
        );
    }

    private record DashboardUser(
        String initials,
        String name,
        String username,
        String meta,
        String status,
        String statusClass
    ) {
    }
}
