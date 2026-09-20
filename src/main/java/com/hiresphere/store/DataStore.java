package com.hiresphere.store;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hiresphere.model.*;
import com.hiresphere.realtime.RealtimeEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * DataStore - Persistent Real-Time Data Store for HireSphere AI.
 * 
 * Provides:
 * 1. 100% Free, zero-setup persistent disk storage under './data/hiresphere_realtime_db.json'.
 * 2. Instant real-time broadcasting via RealtimeEventService (SSE) upon any mutation.
 * 3. Seed demo accounts on initial creation (admin@hiresphere.ai, recruiter@techcorp.com, alice@example.com).
 * 4. Full support for AI analyses and multi-turn AI mock interview conversations.
 */
@Component
public class DataStore {

    private static final Logger log = LoggerFactory.getLogger(DataStore.class);
    private static final String DATA_FILE_PATH = "./data/hiresphere_realtime_db.json";

    private final ObjectMapper mapper;

    @Autowired(required = false)
    private RealtimeEventService realtimeEventService;

    // Collections
    private final List<User> users = Collections.synchronizedList(new ArrayList<>());
    private final List<CandidateProfile> candidateProfiles = Collections.synchronizedList(new ArrayList<>());
    private final List<RecruiterProfile> recruiterProfiles = Collections.synchronizedList(new ArrayList<>());
    private final List<Company> companies = Collections.synchronizedList(new ArrayList<>());
    private final List<Job> jobs = Collections.synchronizedList(new ArrayList<>());
    private final List<Application> applications = Collections.synchronizedList(new ArrayList<>());
    private final List<SavedJob> savedJobs = Collections.synchronizedList(new ArrayList<>());
    private final List<Interview> interviews = Collections.synchronizedList(new ArrayList<>());
    private final List<Notification> notifications = Collections.synchronizedList(new ArrayList<>());
    private final List<AiAnalysis> aiAnalyses = Collections.synchronizedList(new ArrayList<>());
    private final List<AiChatMessage> aiChatMessages = Collections.synchronizedList(new ArrayList<>());

    // ID Counters
    private int userIdCounter = 1;
    private int candidateProfileIdCounter = 1;
    private int recruiterProfileIdCounter = 1;
    private int companyIdCounter = 1;
    private int jobIdCounter = 1;
    private int applicationIdCounter = 1;
    private int savedJobIdCounter = 1;
    private int interviewIdCounter = 1;
    private int notificationIdCounter = 1;
    private int aiAnalysisIdCounter = 1;
    private int aiChatMessageIdCounter = 1;

    // Session Tokens: token -> userId
    private final Map<String, Integer> activeSessions = new ConcurrentHashMap<>();

    public DataStore() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

        loadFromDiskOrSeed();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Disk Persistence (Load & Save)
    // ─────────────────────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DatabaseSnapshot {
        public List<User> users = new ArrayList<>();
        public List<CandidateProfile> candidateProfiles = new ArrayList<>();
        public List<RecruiterProfile> recruiterProfiles = new ArrayList<>();
        public List<Company> companies = new ArrayList<>();
        public List<Job> jobs = new ArrayList<>();
        public List<Application> applications = new ArrayList<>();
        public List<SavedJob> savedJobs = new ArrayList<>();
        public List<Interview> interviews = new ArrayList<>();
        public List<Notification> notifications = new ArrayList<>();
        public List<AiAnalysis> aiAnalyses = new ArrayList<>();
        public List<AiChatMessage> aiChatMessages = new ArrayList<>();

        public int userIdCounter = 1;
        public int candidateProfileIdCounter = 1;
        public int recruiterProfileIdCounter = 1;
        public int companyIdCounter = 1;
        public int jobIdCounter = 1;
        public int applicationIdCounter = 1;
        public int savedJobIdCounter = 1;
        public int interviewIdCounter = 1;
        public int notificationIdCounter = 1;
        public int aiAnalysisIdCounter = 1;
        public int aiChatMessageIdCounter = 1;
    }

    private boolean isTestMode() {
        return System.getProperty("surefire.test.class.path") != null || System.getProperty("hiresphere.storage.persist-to-disk", "true").equals("false");
    }

    private synchronized void loadFromDiskOrSeed() {
        if (isTestMode()) {
            loadSampleData();
            return;
        }

        File file = new File(DATA_FILE_PATH);
        if (file.exists() && file.length() > 0) {
            try {
                DatabaseSnapshot snap = mapper.readValue(file, DatabaseSnapshot.class);
                if (snap.users != null) users.addAll(snap.users);
                if (snap.candidateProfiles != null) candidateProfiles.addAll(snap.candidateProfiles);
                if (snap.recruiterProfiles != null) recruiterProfiles.addAll(snap.recruiterProfiles);
                if (snap.companies != null) companies.addAll(snap.companies);
                if (snap.jobs != null) jobs.addAll(snap.jobs);
                if (snap.applications != null) applications.addAll(snap.applications);
                if (snap.savedJobs != null) savedJobs.addAll(snap.savedJobs);
                if (snap.interviews != null) interviews.addAll(snap.interviews);
                if (snap.notifications != null) notifications.addAll(snap.notifications);
                if (snap.aiAnalyses != null) aiAnalyses.addAll(snap.aiAnalyses);
                if (snap.aiChatMessages != null) aiChatMessages.addAll(snap.aiChatMessages);

                this.userIdCounter = Math.max(snap.userIdCounter, getMaxId(users) + 1);
                this.candidateProfileIdCounter = Math.max(snap.candidateProfileIdCounter, getMaxCandidateProfileId(candidateProfiles) + 1);
                this.recruiterProfileIdCounter = Math.max(snap.recruiterProfileIdCounter, getMaxRecruiterProfileId(recruiterProfiles) + 1);
                this.companyIdCounter = Math.max(snap.companyIdCounter, getMaxCompanyId(companies) + 1);
                this.jobIdCounter = Math.max(snap.jobIdCounter, getMaxJobId(jobs) + 1);
                this.applicationIdCounter = Math.max(snap.applicationIdCounter, getMaxApplicationId(applications) + 1);
                this.savedJobIdCounter = Math.max(snap.savedJobIdCounter, getMaxSavedJobId(savedJobs) + 1);
                this.interviewIdCounter = Math.max(snap.interviewIdCounter, getMaxInterviewId(interviews) + 1);
                this.notificationIdCounter = Math.max(snap.notificationIdCounter, getMaxNotificationId(notifications) + 1);
                this.aiAnalysisIdCounter = Math.max(snap.aiAnalysisIdCounter, getMaxAiAnalysisId(aiAnalyses) + 1);
                this.aiChatMessageIdCounter = Math.max(snap.aiChatMessageIdCounter, getMaxAiChatMessageId(aiChatMessages) + 1);

                log.info("Loaded persistent database from disk: {} users, {} jobs, {} applications", users.size(), jobs.size(), applications.size());
                return;
            } catch (Exception e) {
                log.error("Failed reading database from disk, reseeding: {}", e.getMessage());
            }
        }

        loadSampleData();
        saveToDisk();
    }

    public synchronized void saveToDisk() {
        if (isTestMode()) {
            return;
        }
        try {
            File dir = new File("./data");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            DatabaseSnapshot snap = new DatabaseSnapshot();
            snap.users = new ArrayList<>(users);
            snap.candidateProfiles = new ArrayList<>(candidateProfiles);
            snap.recruiterProfiles = new ArrayList<>(recruiterProfiles);
            snap.companies = new ArrayList<>(companies);
            snap.jobs = new ArrayList<>(jobs);
            snap.applications = new ArrayList<>(applications);
            snap.savedJobs = new ArrayList<>(savedJobs);
            snap.interviews = new ArrayList<>(interviews);
            snap.notifications = new ArrayList<>(notifications);
            snap.aiAnalyses = new ArrayList<>(aiAnalyses);
            snap.aiChatMessages = new ArrayList<>(aiChatMessages);

            snap.userIdCounter = this.userIdCounter;
            snap.candidateProfileIdCounter = this.candidateProfileIdCounter;
            snap.recruiterProfileIdCounter = this.recruiterProfileIdCounter;
            snap.companyIdCounter = this.companyIdCounter;
            snap.jobIdCounter = this.jobIdCounter;
            snap.applicationIdCounter = this.applicationIdCounter;
            snap.savedJobIdCounter = this.savedJobIdCounter;
            snap.interviewIdCounter = this.interviewIdCounter;
            snap.notificationIdCounter = this.notificationIdCounter;
            snap.aiAnalysisIdCounter = this.aiAnalysisIdCounter;
            snap.aiChatMessageIdCounter = this.aiChatMessageIdCounter;

            mapper.writeValue(new File(DATA_FILE_PATH), snap);
        } catch (Exception e) {
            log.error("Error saving database snapshot to disk: {}", e.getMessage());
        }
    }

    private int getMaxId(List<User> list) { return list.stream().mapToInt(User::getId).max().orElse(0); }
    private int getMaxCandidateProfileId(List<CandidateProfile> list) { return list.stream().mapToInt(CandidateProfile::getId).max().orElse(0); }
    private int getMaxRecruiterProfileId(List<RecruiterProfile> list) { return list.stream().mapToInt(RecruiterProfile::getId).max().orElse(0); }
    private int getMaxCompanyId(List<Company> list) { return list.stream().mapToInt(Company::getId).max().orElse(0); }
    private int getMaxJobId(List<Job> list) { return list.stream().mapToInt(Job::getId).max().orElse(0); }
    private int getMaxApplicationId(List<Application> list) { return list.stream().mapToInt(Application::getId).max().orElse(0); }
    private int getMaxSavedJobId(List<SavedJob> list) { return list.stream().mapToInt(SavedJob::getId).max().orElse(0); }
    private int getMaxInterviewId(List<Interview> list) { return list.stream().mapToInt(Interview::getId).max().orElse(0); }
    private int getMaxNotificationId(List<Notification> list) { return list.stream().mapToInt(Notification::getId).max().orElse(0); }
    private int getMaxAiAnalysisId(List<AiAnalysis> list) { return list.stream().mapToInt(AiAnalysis::getId).max().orElse(0); }
    private int getMaxAiChatMessageId(List<AiChatMessage> list) { return list.stream().mapToInt(AiChatMessage::getId).max().orElse(0); }

    // ─────────────────────────────────────────────────────────────────────────
    // Seed Sample Data
    // ─────────────────────────────────────────────────────────────────────────

    private void loadSampleData() {
        // Users: ID 1 = Admin, ID 2 = Recruiter, ID 3 = Candidate
        User admin = new User("Admin", "admin@careerhub.com", "admin123", "ADMIN");
        addUserInternal(admin);

        User recruiter = new User("Rahul Sharma", "recruiter@techcorp.com", "recruiter123", "RECRUITER");
        addUserInternal(recruiter);

        User candidate = new User("Alice Fernandes", "alice@example.com", "candidate123", "CANDIDATE");
        addUserInternal(candidate);

        // Company
        Company company = new Company("TechCorp Solutions", "IT / Software", "Bangalore, India");
        company.setWebsite("https://techcorp.com");
        company.setDescription("A leading enterprise software company building next-gen cloud platforms.");
        company.setSize(500);
        addCompanyInternal(company);

        // Recruiter Profile
        RecruiterProfile recruiterProfile = new RecruiterProfile(recruiter.getId(), company.getId(), "HR Director");
        recruiterProfile.setPhone("+91-9876543210");
        addRecruiterProfileInternal(recruiterProfile);

        // Candidate Profile
        CandidateProfile candidateProfile = new CandidateProfile(candidate.getId(), "Full-Stack Java & React Developer", "Mumbai, India", "B.Tech CSE - Mumbai University (2025)");
        candidateProfile.setBio("Passionate developer eager to build scalable microservices and dynamic user interfaces.");
        candidateProfile.setExperience("Fresher");
        candidateProfile.setPhone("+91-9123456789");
        candidateProfile.setSkills(new ArrayList<>(Arrays.asList("Java", "Spring Boot", "React", "MySQL", "REST API", "Git")));
        addCandidateProfileInternal(candidateProfile);

        // Jobs
        Job job1 = new Job(recruiter.getId(), company.getId(), "Java Backend Developer", "Bangalore, India", "FULL_TIME");
        job1.setCompany(company.getName());
        job1.setDescription("Build high-performance microservices, REST APIs, and event-driven backends using Java and Spring Boot.");
        job1.setRequirements("Strong OOP knowledge, Core Java, Spring Boot, REST API design, Git");
        job1.setSalaryRange("₹4-6 LPA");
        job1.setExperience("Fresher / 0-1 year");
        job1.setSkills(new ArrayList<>(Arrays.asList("Java", "Spring Boot", "REST API", "MySQL")));
        job1.setActive(true);
        job1.setDeadline(LocalDate.now().plusDays(30));
        addJobInternal(job1);

        Job job2 = new Job(recruiter.getId(), company.getId(), "React Frontend Intern", "Remote", "INTERNSHIP");
        job2.setCompany(company.getName());
        job2.setDescription("Paid internship building modern React interfaces and responsive user experiences.");
        job2.setRequirements("HTML5, CSS3, JavaScript, React hooks, component architecture");
        job2.setSalaryRange("₹20,000/month stipend");
        job2.setExperience("Fresher");
        job2.setSkills(new ArrayList<>(Arrays.asList("React", "JavaScript", "HTML", "CSS")));
        job2.setActive(true);
        job2.setDeadline(LocalDate.now().plusDays(20));
        addJobInternal(job2);

        Job job3 = new Job(recruiter.getId(), company.getId(), "Full Stack AI Engineer", "Pune, India", "FULL_TIME");
        job3.setCompany(company.getName());
        job3.setDescription("Build intelligent web applications integrating LLMs, real-time event streaming, and reactive frontends.");
        job3.setRequirements("Java, Spring Boot, React, Python/AI APIs, SQL");
        job3.setSalaryRange("₹8-14 LPA");
        job3.setExperience("0-2 years");
        job3.setSkills(new ArrayList<>(Arrays.asList("Java", "Spring Boot", "React", "REST API", "Git")));
        job3.setActive(true);
        job3.setDeadline(LocalDate.now().plusDays(25));
        addJobInternal(job3);

        // Application
        Application app1 = new Application(candidate.getId(), job1.getId(), "I am passionate about modern backend development with Spring Boot!");
        app1.setJobTitle(job1.getTitle());
        app1.setCompany(job1.getCompany());
        app1.setCandidateName(candidate.getName());
        app1.setCandidateEmail(candidate.getEmail());
        app1.setCandidatePhone(candidateProfile.getPhone());
        app1.setStatus("INTERVIEW_SCHEDULED");
        addApplicationInternal(app1);

        // Saved Job
        SavedJob savedJob = new SavedJob(candidate.getId(), job2.getId());
        savedJob.setJobTitle(job2.getTitle());
        savedJob.setCompany(job2.getCompany());
        savedJob.setLocation(job2.getLocation());
        savedJob.setSalaryRange(job2.getSalaryRange());
        savedJob.setEmploymentType(job2.getEmploymentType());
        savedJob.setJob(job2);
        addSavedJobInternal(savedJob);

        // Interview
        Interview interview = new Interview(app1.getId(), candidate.getId(), recruiter.getId(), job1.getId(), LocalDateTime.now().plusDays(5), "ONLINE");
        interview.setDate(LocalDate.now().plusDays(5).toString());
        interview.setTime("11:00 AM");
        interview.setMeetingLink("https://meet.google.com/hiresphere-demo");
        interview.setLocation("https://meet.google.com/hiresphere-demo");
        interview.setNotes("Please join 5 minutes early. Coding assessment will follow behavioral discussion.");
        interview.setJobTitle(job1.getTitle());
        interview.setCompany(job1.getCompany());
        interview.setCandidateName(candidate.getName());
        interview.setCandidateEmail(candidate.getEmail());
        interview.setRecruiterName(recruiter.getName());
        addInterviewInternal(interview);

        // Notifications
        Notification n1 = new Notification(candidate.getId(), "APPLICATION_UPDATE", "Your application for 'Java Backend Developer' has been shortlisted!");
        addNotificationInternal(n1);

        Notification n2 = new Notification(candidate.getId(), "INTERVIEW_SCHEDULED", "Interview scheduled for 'Java Backend Developer' on " + LocalDate.now().plusDays(5) + " via Google Meet.");
        addNotificationInternal(n2);

        // Seeded AI Analysis
        AiAnalysis seededAnalysis = new AiAnalysis(candidate.getId(), job1.getId(), 92, "EXCELLENT_FIT", "Candidate possesses strong proficiency in core Java and Spring Boot ecosystem matching 100% of primary requirements.");
        seededAnalysis.setStrengths(List.of("Strong Java & Spring Boot mastery", "Solid REST API fundamentals", "Hands-on Git version control"));
        seededAnalysis.setSkillGaps(List.of("Microservices containerization (Docker) could be enhanced"));
        seededAnalysis.setRecommendedAction("Fast-track to technical interview round.");
        seededAnalysis.setSuggestedInterviewQuestions(List.of(
                "How do you design idempotent RESTful POST endpoints in Spring Boot?",
                "Explain how Spring Dependency Injection works under the hood.",
                "How do you handle transactional rollbacks across multiple service calls?"
        ));
        addAiAnalysisInternal(seededAnalysis);
    }

    private void addUserInternal(User user) { user.setId(userIdCounter++); users.add(user); }
    private void addCompanyInternal(Company c) { c.setId(companyIdCounter++); companies.add(c); }
    private void addCandidateProfileInternal(CandidateProfile cp) { cp.setId(candidateProfileIdCounter++); candidateProfiles.add(cp); }
    private void addRecruiterProfileInternal(RecruiterProfile rp) { rp.setId(recruiterProfileIdCounter++); recruiterProfiles.add(rp); }
    private void addJobInternal(Job j) { j.setId(jobIdCounter++); jobs.add(j); }
    private void addApplicationInternal(Application a) { a.setId(applicationIdCounter++); applications.add(a); }
    private void addSavedJobInternal(SavedJob sj) { sj.setId(savedJobIdCounter++); savedJobs.add(sj); }
    private void addInterviewInternal(Interview i) { i.setId(interviewIdCounter++); interviews.add(i); }
    private void addNotificationInternal(Notification n) { n.setId(notificationIdCounter++); notifications.add(n); }
    private void addAiAnalysisInternal(AiAnalysis a) { a.setId(aiAnalysisIdCounter++); aiAnalyses.add(a); }

    // ─────────────────────────────────────────────────────────────────────────
    // Real-Time Persistent CRUD Methods
    // ─────────────────────────────────────────────────────────────────────────

    public List<User> getUsers() { return new ArrayList<>(users); }

    public void addUser(User user) {
        user.setId(userIdCounter++);
        users.add(user);
        saveToDisk();
        notifyEvent("USER_REGISTERED", "USER", user.getId(), "User registered: " + user.getName(), user);
    }

    public User findUserById(int userId) {
        return users.stream().filter(u -> u.getId() == userId).findFirst().orElse(null);
    }

    public List<User> findUsersByRole(String role) {
        if (role == null || role.isBlank()) return new ArrayList<>(users);
        return users.stream().filter(u -> role.equalsIgnoreCase(u.getRole())).collect(Collectors.toList());
    }

    public boolean updateUserStatus(int userId, boolean active) {
        User user = findUserById(userId);
        if (user != null) {
            user.setActive(active);
            saveToDisk();
            notifyEvent("USER_STATUS_UPDATED", "USER", userId, "User status updated: " + user.getEmail(), user);
            return true;
        }
        return false;
    }

    // Companies
    public List<Company> getCompanies() { return new ArrayList<>(companies); }

    public void addCompany(Company company) {
        company.setId(companyIdCounter++);
        companies.add(company);
        saveToDisk();
    }

    public Company findCompanyById(int companyId) {
        return companies.stream().filter(c -> c.getId() == companyId).findFirst().orElse(null);
    }

    // Candidate Profiles
    public List<CandidateProfile> getCandidateProfiles() { return new ArrayList<>(candidateProfiles); }

    public void addCandidateProfile(CandidateProfile profile) {
        profile.setId(candidateProfileIdCounter++);
        candidateProfiles.add(profile);
        saveToDisk();
        notifyEvent("PROFILE_UPDATED", "CANDIDATE_PROFILE", profile.getId(), "Candidate profile updated", profile);
    }

    public CandidateProfile findCandidateProfileByUserId(int userId) {
        return candidateProfiles.stream().filter(p -> p.getUserId() == userId).findFirst().orElse(null);
    }

    // Recruiter Profiles
    public List<RecruiterProfile> getRecruiterProfiles() { return new ArrayList<>(recruiterProfiles); }

    public void addRecruiterProfile(RecruiterProfile profile) {
        profile.setId(recruiterProfileIdCounter++);
        recruiterProfiles.add(profile);
        saveToDisk();
    }

    public RecruiterProfile findRecruiterProfileByUserId(int userId) {
        return recruiterProfiles.stream().filter(p -> p.getUserId() == userId).findFirst().orElse(null);
    }

    // Jobs
    public List<Job> getJobs() { return new ArrayList<>(jobs); }

    public void addJob(Job job) {
        job.setId(jobIdCounter++);
        jobs.add(job);
        saveToDisk();
        notifyEvent("JOB_POSTED", "JOB", job.getId(), "New job posted: " + job.getTitle(), job);
    }

    public int nextJobId() { return jobIdCounter; }

    public Job findJobById(int jobId) {
        return jobs.stream().filter(j -> j.getId() == jobId).findFirst().orElse(null);
    }

    public boolean removeJob(int jobId) {
        boolean removed = jobs.removeIf(j -> j.getId() == jobId);
        if (removed) {
            saveToDisk();
            notifyEvent("JOB_REMOVED", "JOB", jobId, "Job removed: #" + jobId, null);
        }
        return removed;
    }

    public boolean updateJobStatus(int jobId, boolean active) {
        Job job = findJobById(jobId);
        if (job != null) {
            job.setActive(active);
            saveToDisk();
            notifyEvent("JOB_STATUS_UPDATED", "JOB", jobId, "Job status changed: " + job.getTitle(), job);
            return true;
        }
        return false;
    }

    // Applications
    public List<Application> getApplications() { return new ArrayList<>(applications); }

    public void addApplication(Application application) {
        application.setId(applicationIdCounter++);
        applications.add(application);
        saveToDisk();
        notifyEvent("APPLICATION_SUBMITTED", "APPLICATION", application.getId(), "New application for " + application.getJobTitle(), application);
    }

    public Application findApplicationById(int id) {
        return applications.stream().filter(a -> a.getId() == id).findFirst().orElse(null);
    }

    public List<Application> findApplicationsByCandidateId(int candidateId) {
        return applications.stream().filter(a -> a.getCandidateId() == candidateId).collect(Collectors.toList());
    }

    public List<Application> findApplicationsByJobId(int jobId) {
        return applications.stream().filter(a -> a.getJobId() == jobId).collect(Collectors.toList());
    }

    public boolean hasCandidateApplied(int candidateId, int jobId) {
        return applications.stream().anyMatch(a -> a.getCandidateId() == candidateId && a.getJobId() == jobId);
    }

    // Saved Jobs
    public List<SavedJob> getSavedJobs() { return new ArrayList<>(savedJobs); }

    public void addSavedJob(SavedJob savedJob) {
        savedJob.setId(savedJobIdCounter++);
        savedJobs.add(savedJob);
        saveToDisk();
    }

    public void removeSavedJob(int id) {
        savedJobs.removeIf(sj -> sj.getId() == id);
        saveToDisk();
    }

    public SavedJob findSavedJobById(int id) {
        return savedJobs.stream().filter(sj -> sj.getId() == id).findFirst().orElse(null);
    }

    public List<SavedJob> findSavedJobsByCandidateId(int candidateId) {
        return savedJobs.stream().filter(sj -> sj.getCandidateId() == candidateId).collect(Collectors.toList());
    }

    public boolean isJobSavedByCandidate(int candidateId, int jobId) {
        return savedJobs.stream().anyMatch(sj -> sj.getCandidateId() == candidateId && sj.getJobId() == jobId);
    }

    public boolean removeSavedJobByCandidateAndJob(int candidateId, int jobId) {
        boolean removed = savedJobs.removeIf(sj -> sj.getCandidateId() == candidateId && sj.getJobId() == jobId);
        if (removed) saveToDisk();
        return removed;
    }

    public SavedJob findSavedJobByCandidateAndJob(int candidateId, int jobId) {
        return savedJobs.stream().filter(sj -> sj.getCandidateId() == candidateId && sj.getJobId() == jobId).findFirst().orElse(null);
    }

    // Interviews
    public List<Interview> getInterviews() { return new ArrayList<>(interviews); }

    public void addInterview(Interview interview) {
        interview.setId(interviewIdCounter++);
        interviews.add(interview);
        saveToDisk();
        notifyEvent("INTERVIEW_SCHEDULED", "INTERVIEW", interview.getId(), "Interview scheduled for " + interview.getJobTitle(), interview);
    }

    public Interview findInterviewById(int id) {
        return interviews.stream().filter(i -> i.getId() == id).findFirst().orElse(null);
    }

    public List<Interview> findInterviewsByCandidateId(int candidateId) {
        return interviews.stream().filter(i -> i.getCandidateId() == candidateId).collect(Collectors.toList());
    }

    public List<Interview> findInterviewsByRecruiterId(int recruiterId) {
        return interviews.stream().filter(i -> i.getRecruiterId() == recruiterId).collect(Collectors.toList());
    }

    public List<Interview> findInterviewsByApplicationId(int applicationId) {
        return interviews.stream().filter(i -> i.getApplicationId() == applicationId).collect(Collectors.toList());
    }

    public boolean removeInterview(int id) {
        boolean removed = interviews.removeIf(i -> i.getId() == id);
        if (removed) saveToDisk();
        return removed;
    }

    // Notifications
    public List<Notification> getNotifications() { return new ArrayList<>(notifications); }

    public void addNotification(Notification notification) {
        notification.setId(notificationIdCounter++);
        notifications.add(notification);
        saveToDisk();
        notifyEvent("NOTIFICATION_SENT", "NOTIFICATION", notification.getId(), notification.getMessage(), notification);
    }

    public Notification findNotificationById(int id) {
        return notifications.stream().filter(n -> n.getId() == id).findFirst().orElse(null);
    }

    public List<Notification> findNotificationsByUserId(int userId) {
        return notifications.stream().filter(n -> n.getUserId() == userId).collect(Collectors.toList());
    }

    public boolean markNotificationAsRead(int id) {
        Notification notification = findNotificationById(id);
        if (notification != null) {
            notification.setRead(true);
            saveToDisk();
            return true;
        }
        return false;
    }

    public int markAllNotificationsAsReadForUser(int userId) {
        int count = 0;
        for (Notification n : notifications) {
            if (n.getUserId() == userId && !n.isRead()) {
                n.setRead(true);
                count++;
            }
        }
        if (count > 0) saveToDisk();
        return count;
    }

    // AI Analyses
    public List<AiAnalysis> getAiAnalyses() { return new ArrayList<>(aiAnalyses); }

    public void addAiAnalysis(AiAnalysis analysis) {
        analysis.setId(aiAnalysisIdCounter++);
        aiAnalyses.add(analysis);
        saveToDisk();
    }

    public List<AiAnalysis> findAiAnalysesByJobId(int jobId) {
        return aiAnalyses.stream().filter(a -> a.getJobId() == jobId).collect(Collectors.toList());
    }

    public List<AiAnalysis> findAiAnalysesByCandidateId(int candidateId) {
        return aiAnalyses.stream().filter(a -> a.getCandidateId() == candidateId).collect(Collectors.toList());
    }

    // AI Chat Messages
    public List<AiChatMessage> getAiChatMessages() { return new ArrayList<>(aiChatMessages); }

    public void addAiChatMessage(AiChatMessage msg) {
        msg.setId(aiChatMessageIdCounter++);
        aiChatMessages.add(msg);
        saveToDisk();
    }

    public List<AiChatMessage> findAiChatMessagesBySessionId(String sessionId) {
        return aiChatMessages.stream().filter(m -> sessionId.equals(m.getSessionId())).collect(Collectors.toList());
    }

    // Session Tokens
    public void saveSession(String token, int userId) { activeSessions.put(token, userId); }
    public int getUserIdByToken(String token) { return activeSessions.getOrDefault(token, -1); }
    public void removeSession(String token) { activeSessions.remove(token); }
    public boolean isValidToken(String token) { return activeSessions.containsKey(token); }

    private void notifyEvent(String eventType, String entityType, int entityId, String message, Object payload) {
        if (realtimeEventService != null) {
            try {
                realtimeEventService.broadcast(eventType, entityType, entityId, message, payload);
            } catch (Exception ignored) {}
        }
    }
}
