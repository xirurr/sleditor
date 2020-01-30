package Mail;

import Base.ProjectConf;

import java.util.List;

public interface MailService {
    public void sendMail();
    public void sendMail(List<ProjectConf> list);
}
