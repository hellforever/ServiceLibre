package com.arcsolu.sopda.biz;

public interface IDB {

    boolean CreateDB();
    boolean Connect();
    void Disconnect();
    
    boolean StartTransAction();
    boolean CommitTransAction();
    boolean RollbackTransAction();
    boolean BackupTo();
        //IDao *CreateDao(DaoType type)=0;
	boolean IsValid();
}
