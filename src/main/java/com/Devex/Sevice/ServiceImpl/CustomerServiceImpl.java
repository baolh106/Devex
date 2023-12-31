package com.Devex.Sevice.ServiceImpl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.Devex.Entity.ImageProduct;
import com.Devex.Sevice.ImageProductService;
import com.Devex.Sevice.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import com.Devex.Entity.Customer;
import com.Devex.Repository.CustomerRepository;
import com.Devex.Sevice.CustomerService;
import org.springframework.web.multipart.MultipartFile;

@SessionScope
@Service("customerService")
public class CustomerServiceImpl implements CustomerService{
	@Autowired
	CustomerRepository customerRepository;

	@Override
	public Customer save(Customer entity) {
		return customerRepository.save(entity);
	}

	@Override
	public List<Customer> saveAll(List<Customer> entities) {
		return customerRepository.saveAll(entities);
	}

	@Override
	public Optional<Customer> findOne(Example<Customer> example) {
		return customerRepository.findOne(example);
	}

	@Override
	public List<Customer> findAll(Sort sort) {
		return customerRepository.findAll(sort);
	}

	@Override
	public Page<Customer> findAll(Pageable pageable) {
		return customerRepository.findAll(pageable);
	}

	@Override
	public List<Customer> findAll() {
		return customerRepository.findAll();
	}

	@Override
	public List<Customer> findAllById(Iterable<String> ids) {
		return customerRepository.findAllById(ids);
	}

	@Override
	public Optional<Customer> findById(String id) {
		return customerRepository.findById(id);
	}

	@Override
	public long count() {
		return customerRepository.count();
	}

	@Override
	public void deleteById(String id) {
		customerRepository.deleteById(id);
	}

	@Override
	public Customer getById(String id) {
		return customerRepository.getById(id);
	}

	@Override
	public void delete(Customer entity) {
		customerRepository.delete(entity);
	}

	@Override
	public void deleteAll() {
		customerRepository.deleteAll();
	}


    @Service
    public static class FileManagerService {
        @Value("${myapp.file-storage-path}")
        private String fileStoragePath;

        @Autowired
        SessionService session;

        @Autowired
        ImageProductService imageProductService;

        private Path getPath(String shopname, String id, String filename) {
            File shop = Paths.get(fileStoragePath + "/product", shopname).toFile();
            if(!shop.exists()) {
                shop.mkdirs();
            }
            File dir = Paths.get(fileStoragePath + "/product", shopname, id).toFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return Paths.get(dir.getAbsolutePath(), filename);
        }

        public byte[] read(String shopname, String id, String filename) {
            Path path = this.getPath(shopname, id, filename);
            try {
                return Files.readAllBytes(path);
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }

        public List<String> save(String shopname, String id, MultipartFile[] files) {
            List<String> filenames = new ArrayList<String>();
            for (MultipartFile file : files) {
                String name = System.currentTimeMillis() + file.getOriginalFilename();
                String filename = Integer.toHexString(name.hashCode()) + name.substring(name.lastIndexOf("."));
                Path path = this.getPath(shopname, id, filename);
                try {
                    file.transferTo(path);
                    filenames.add(filename);
                    imageProductService.insertImageProduct(id, filename, id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            session.set("listimg", filenames);
            return filenames;
        }

        public void delete(String shopname, String id, String filename) {
            Path path = this.getPath(shopname, id, filename);
            path.toFile().delete();
        }

        public List<String> list(String id, String shopname) {
            List<ImageProduct> listImageProduct = imageProductService.findAllImageProductByProductId(id);
            List<String> filenames = new ArrayList<String>();
            File dir = Paths.get(fileStoragePath + "/product", shopname, id).toFile();
            File shop = Paths.get(fileStoragePath + "/product", shopname).toFile();
            System.out.println(dir.getAbsolutePath());
            if(!shop.exists()) {
                shop.mkdirs();

                if(!dir.exists()){
                    dir.mkdirs();
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        filenames.add("/img/product/"+file.getName());
                    }
                }
            }else {
                if (dir.exists() && listImageProduct.size() > 0) {
                    System.out.println(listImageProduct.size());
                    System.out.println(id+"check2");
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        filenames.add("/img/product/"+file.getName());
                    }
                } else if (dir.exists() && listImageProduct.size() == 0) {
                    System.out.println(id+"check1");
                    File file = Paths.get(fileStoragePath + "/product", shopname).toFile();
                    Path path = Paths.get(file.getAbsolutePath(), id);
                    path.toFile().delete();
                }else if(!dir.exists()) {
                    System.out.println(id+"check");
                    dir.mkdirs();
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        filenames.add("/img/product/"+file.getName());
                    }
                }
            }
            return filenames;
        }

        public void changeImage(String shopname, String id) {
            Path source = Paths.get(fileStoragePath + "/product", "default.webp");
            Path target = this.getPath(shopname, id, "default.webp");
            try {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
