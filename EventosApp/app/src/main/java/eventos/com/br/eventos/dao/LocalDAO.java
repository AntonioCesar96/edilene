package eventos.com.br.eventos.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

import eventos.com.br.eventos.model.Faculdade;
import eventos.com.br.eventos.model.Local;

/**
 * Created by antonio on 12/03/17.
 */

public class LocalDAO extends BaseDaoImpl<Local, Long> {
    public LocalDAO(ConnectionSource cs) throws SQLException{
        super(Local.class);
        setConnectionSource(cs);
        initialize();
    }

    public void save(Local l) throws SQLException {
        if (l != null) {
            if (!idExists(l.getId())){
                create(l);
            }

            new CidadeDAO(connectionSource).save(l.getCidade());
        }
    }
}